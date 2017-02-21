package io.nayuki.qrcodegen;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.bind.DatatypeConverter;

public class GeneratorFrame extends JFrame {

	private final JButton _GenerateButton = new JButton("Generate QR Code");
	protected JTextField textField = new JTextField(50);
	// File imgFile = new File("QRCode.png");
	SecretKey secKey = getSecretEncryptionKey();
	
	public GeneratorFrame() throws Exception {
		super("QR Generator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		JPanel textPanel = new JPanel(new FlowLayout());
		textPanel.add(textField);
		textPanel.add(_GenerateButton);

		add(textPanel, BorderLayout.SOUTH);
		JLabel QRImage = new JLabel();
		add(QRImage, BorderLayout.CENTER);
		//SecretKey secKey = getSecretEncryptionKey();

		_GenerateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textField.getText();
				byte[] cipherText = null;
				try {
					cipherText = encryptText(text, secKey);
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				System.out.print("Message to be encrypted is \n");
				System.out.print(text + " \n");
				String encryptedTextInHex = bytesToHex(cipherText);
				System.out.print("Encrypted message: \n");
				System.out.print(encryptedTextInHex);
				System.out.print("\n");
				byte[] attemptToDecrypt = hexToBytes(encryptedTextInHex);
				String decryptedText = null;
				try {
					decryptedText = decryptText(attemptToDecrypt, secKey);
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				System.out.print("Decrypted message: \n");
				System.out.print(decryptedText);
				try {
					doBasicDemo(encryptedTextInHex);
					BufferedImage image = ImageIO.read(new File("QRCode.png"));
					QRImage.setIcon(new ImageIcon(image));

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		// set the size of the panel
		setSize(800, 500);
	}

	private static void doBasicDemo(String arg) throws IOException {
		// String text = arg; // User-supplied Unicode text
		QrCode.Ecc errCorLvl = QrCode.Ecc.LOW; // Error correction level

		QrCode qr = QrCode.encodeText(arg, errCorLvl); // Make the QR Code
														// symbol

		BufferedImage img = qr.toImage(10, 4); // Convert to bitmap image
		File imgFile = new File("QRCode.png"); // File path for output
		ImageIO.write(img, "png", imgFile); // Write image to file

		/*
		 * String svg = qr.toSvgString(4); // Convert to SVG XML code try
		 * (Writer out = new OutputStreamWriter( new
		 * FileOutputStream("hello-world-QR.svg"), StandardCharsets.UTF_8)) {
		 * out.write(svg); // Create/overwrite file and write SVG data }
		 */
	}

	public static SecretKey getSecretEncryptionKey() throws Exception {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(128); // The AES key size in number of bits
		SecretKey secKey = generator.generateKey();
		return secKey;
	}

	/**
	 * Encrypts plainText in AES using the secret key
	 * 
	 * @param plainText
	 * @param secKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptText(String plainText, SecretKey secKey) throws Exception {
		// AES defaults to AES/ECB/PKCS5Padding in Java 7
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
		byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
		return byteCipherText;
	}

	/**
	 * Decrypts encrypted byte array using the key used for encryption.
	 * 
	 * @param byteCipherText
	 * @param secKey
	 * @return
	 * @throws Exception
	 */
	public static String decryptText(byte[] byteCipherText, SecretKey secKey) throws Exception {
		// AES defaults to AES/ECB/PKCS5Padding in Java 7
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.DECRYPT_MODE, secKey);
		byte[] bytePlainText = aesCipher.doFinal(byteCipherText);
		return new String(bytePlainText);
	}

	/**
	 * Convert a binary byte array into readable hex form
	 * 
	 * @param hash
	 * @return
	 */
	private static String bytesToHex(byte[] hash) {
		return DatatypeConverter.printHexBinary(hash);
	}
	
	private static byte[] hexToBytes(String hex){
		return DatatypeConverter.parseHexBinary(hex);
	}
}

