package wallettemplate;

import java.io.IOException; 
import java.net.URI; 
import java.net.URISyntaxException; 
import java.awt.Desktop;

public class VoteResult {//��ǥ ��Ȳ Ȯ�� �������� �̵�
	public static void start() {
		try { 
			Desktop.getDesktop().browse(new URI("http://192.168.219.124/result/index.php"));
			}
		catch (IOException e) {
			e.printStackTrace(); 
			} 
		catch (URISyntaxException e) {
			e.printStackTrace(); 
			}

	}

}
