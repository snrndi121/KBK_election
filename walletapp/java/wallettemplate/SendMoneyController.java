/*
 * Copyright by the original author or authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//중요!!! transaction 나오심다
package wallettemplate;

import javafx.scene.layout.HBox;

import org.bitcoinj.core.*;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.sun.javafx.collections.MappingChange.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.spongycastle.crypto.params.KeyParameter;
import wallettemplate.controls.BitcoinAddressValidator;
import wallettemplate.utils.TextFieldValidator;
import wallettemplate.utils.WTUtils;

import static com.google.common.base.Preconditions.checkState;
import static wallettemplate.utils.GuiUtils.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.annotation.Nullable;

public class SendMoneyController implements Initializable {
    public Button sendBtn;
    public Button cancelBtn;
    public TextField address;
    public Label titleLabel;
    public TextField amountEdit;
    public Label btcLabel;

    public Main.OverlayUI overlayUI;

    private Wallet.SendResult sendResult;
    private KeyParameter aesKey;
    //////////////////////////////////////////////////////////////////////////////
    //hashmap 후보자 이름,주소 세팅을 위함
    public HashMap<String,String> Hmap = new HashMap<String,String>();
    @FXML
    private ComboBox<String> Caddress;//콤보박스
    private ObservableList<String> list = FXCollections.observableArrayList();
    
    // Called by FXMLLoader
    public void initialize() {//maincontroller에서 불러줌 , 콤보박스 때문에 실 동작은 아래의 initialize가 한다
        Coin balance = Main.bitcoin.wallet().getBalance();//getbalance = AVAILABLE찾음
        checkState(!balance.isZero());
        new TextFieldValidator(amountEdit, text ->
                !WTUtils.didThrow(() -> checkState(Coin.parseCoin(text).compareTo(balance) <= 0)));
        amountEdit.setText(balance.toPlainString());
        hinit();
    }
    
    public void cancel(ActionEvent event) {
        overlayUI.done();
    }

    public void send(ActionEvent event) {//중요!!
        // Address exception cannot happen as we validated it beforehand.
    	// 주소 유효성 검사는 사전에 유효성을 검사 했으므로 발생할 수 없습니다.
        try {
            Coin amount = Coin.parseCoin("0.01");
            Address destination = Address.fromBase58(Main.params, Hmap.get(Caddress.getValue()));
            System.out.println("address: " + destination);
            SendRequest req;
            if (amount.equals(Main.bitcoin.wallet().getBalance()))
                req = SendRequest.emptyWallet(destination);
            else
                req = SendRequest.to(destination, amount);
            req.aesKey = aesKey;
            //fee수정
            //req.feePerKb =Coin.ZERO;
            //req.ensureMinRequiredFee = false;
            sendResult = Main.bitcoin.wallet().sendCoins(req);
            System.out.println(sendResult.tx.toString());
            Futures.addCallback(sendResult.broadcastComplete, new FutureCallback<Transaction>() {
                @Override
                public void onSuccess(@Nullable Transaction result) {
                    checkGuiThread();
                    overlayUI.done();
                    //informationalAlert("","성공");
                }

                @Override
                public void onFailure(Throwable t) {
                    // We died trying to empty the wallet.
                    crashAlert(t);
                }
            });
            sendResult.tx.getConfidence().addEventListener((tx, reason) -> {
                if (reason == TransactionConfidence.Listener.ChangeReason.SEEN_PEERS)
                    updateTitleForBroadcast();
            });
            updateTitleForBroadcast();
        }catch (InsufficientMoneyException e) {
            informationalAlert("Could not empty the wallet",
                    "You may have too little money left in the wallet to make a transaction.",0);
            overlayUI.done();
        } catch (ECKey.KeyIsEncryptedException e) {
        }
    }


    private void updateTitleForBroadcast() {
        final int peers = sendResult.tx.getConfidence().numBroadcastPeers();
        //titleLabel.setText(String.format("Broadcasting ... seen by %d peers", peers));
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {//실 동작
		Coin balance = Main.bitcoin.wallet().getBalance();//getbalance = AVAILABLE찾음
        checkState(!balance.isZero()); //이 페이지로 넘어온 시점이라면 Coin이 0이 아니지만 한번 더 검사
        hinit();//후보자 주소 세팅
        Caddress.setItems(list);
        //비트코인 주소의 유효성 검사, 따라서 실제 send에서는 유효성을 검사하지 않는다
        new BitcoinAddressValidator(Main.params, Caddress,Hmap, sendBtn);//만약 유효성 검사에 실패인 경우 send버튼이 비활성화
        new TextFieldValidator(amountEdit, text ->
              !WTUtils.didThrow(() -> checkState(Coin.parseCoin(text).compareTo(balance) <= 0)));
        amountEdit.setText(balance.toPlainString());
	}
	
	public void hinit() {//인증 후 받아온 후보자,주소 세팅
		list=Main.Clist;
		list.add("후보자 선택");
		Hmap.put("후보자 선택","0");//default값
		for(int i=0; i<Main.Clist.size(); i++) {
			Hmap.put(Main.Clist.get(i),Main.CAlist.get(i));
		}
	}
}
