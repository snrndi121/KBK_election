/*
 * Copyright by the original author or authors.
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

package wallettemplate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sun.rmi.runtime.Log;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static wallettemplate.utils.GuiUtils.*;

public class CertificationController {//���� ����
    private static final Logger log = LoggerFactory.getLogger(CertificationController.class);
    public Button closeButton;
    public Main.OverlayUI overlayUI;
    public TextField Fname, Fbirth;
    public String Faddr;
    public String[] Candi;

    // Note: NOT called by FXMLLoader!
    public void initialize() throws Exception{
    	Faddr=Main.bitcoin.wallet().currentReceiveAddress().toString();
    }
    
    @FXML
    public void setaccessClicked(ActionEvent event) throws IOException {
    		 startAs();
    }
	public void closeClicked(ActionEvent event) {//close��ư
        overlayUI.done();
    }
	
	
	//���� ���� ���� .. ���� �����ϴµ�..
	public void startAs() throws IOException{
		URL url = new URL("http://13.124.112.35/KBK_election/server.setting/index.php");//�������� �ּ�
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		 conn.setRequestMethod("POST");
		 conn.setDoOutput(true);
		  
		 try (OutputStream out = conn.getOutputStream()) {//Post�� �̸�,�ֹι�ȣ Post
	            out.write(("u_name="+URLEncoder.encode(Fname.getText(),"UTF-8")).getBytes());//u_name = �±�
	            out.write("&".getBytes()); 
	            out.write(("u_reg=" + URLEncoder.encode(Fbirth.getText(),"UTF-8")).getBytes());//u_reg = �±� ���� ��������
	        }
		InputStream is = conn.getInputStream();
		Scanner scan = new Scanner(is,"UTF-8");
		
		
 		//�������� �ߴ� ���� �� ���ϴ� �κи� ���ͼ� ���(��������+Vcode(�����ڵ�,����,����)+�ĺ����̸�)
		int line = 1;
		String res = null;
		while(scan.hasNext()) {
			String str3 = scan.nextLine();
			if(line == 12) {
			System.out.println("str1 :" + str3);
			res = str3;
			res.substring(0,1);
			str3 = scan.nextLine();
			System.out.println("str2 :" +str3);
			Main.Vcode = str3;
			str3 = scan.nextLine();
			System.out.println("str3 :" +str3);
			Main.Cname = str3;
			Candi = str3.split(",");
			break;
			}
			line++;
			}
		scan.close();
		//�ĺ��� �̸� ���� Test �ʿ�
		for(int i=0; i<Candi.length; i++) {
			Main.Clist.add(Candi[i]);
		}
		//�������ο� ���� ����
		if(res != "0") {
			informationalAlert("��ϵ� ����� �Դϴ�","����",0);
			try {
				//��ǥ�� ��û, ������ ���� �ĺ��� �ּ� ��û
				Voter.start();
				//Json Parser������ ����ó��
			} catch (ParseException e) {
				e.printStackTrace();
			}
		    overlayUI.done();
		    }
		else{//���� 0 or 4�� ����
		    informationalAlert("��ϵ� ����ڰ� �ƴմϴ�","����",0);
		    return;
		    }
		conn.disconnect(); 
		} 
	}