package io.nayuki.qrcodegen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.xml.bind.DatatypeConverter;

public class GeneratorFrame extends JFrame {

	//These are all Frame buttons and labels
	private final JButton _GenerateButton = new JButton("Generate QR Code");
	protected JTextField textField = new JTextField(50);
	protected JLabel inputText = new JLabel("Type message to encode (less than 200 characters)");
	// File imgFile = new File("QRCode.png");
	
	//Variables for filepaths for the public and private keys.
	String publicKeyFilePath = "PublicKey.key";
	String privateKeyFilePath = "PrivateKey.key";
	
	//This is the secKey for the AES encryption.  Move this to the action listener when you figure
	//out how to append it to the message.  Needs to be new each message.
	
	SecretKey secKey = getSecretEncryptionKey();
	byte[] secKeyBytes = secKey.getEncoded();
	String secKeyString = bytesToHex(secKeyBytes);
	

	public GeneratorFrame() throws Exception {
		super("QR Generator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		JPanel textPanel = new JPanel(new FlowLayout());
		textPanel.setPreferredSize(new Dimension(100, 65));
		textPanel.add(inputText);
		textPanel.add(textField);
		textPanel.add(_GenerateButton);

		add(textPanel, BorderLayout.SOUTH);
		JLabel QRImage = new JLabel();
		add(QRImage, BorderLayout.CENTER);
		// SecretKey secKey = getSecretEncryptionKey();
		
		_GenerateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textField.getText();
				if (text.length() > 200) {
					JOptionPane.showMessageDialog(null, "There is more than 200 Characters!");
				} else {

					//fill out text to be 200 characters in length
					String formattedText = String.format("%1$-200s", text);
					byte[] cipherText = null;
					try {
						cipherText = encryptText(formattedText, secKey);
					} catch (Exception e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					System.out.print("Message to be encrypted is: " + formattedText + "\n");
					System.out.print("Size of Messagee is: " + formattedText.length() + "\n");
					String encryptedTextInHex = bytesToHex(cipherText);
					System.out.print("Encrypted message: " + encryptedTextInHex + "\n");
					System.out.print("Size of Encrypted message: " + encryptedTextInHex.length() + "\n");
					
					//Encrypt the AES Key
					byte [] secKeyBytes = secKey.getEncoded();
					try {
						encryptRSA(secKeyBytes);
					} catch (Exception e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					//convert AES Key to string
					String encryptedAESinHex = bytesToHex(secKeyBytes);
					
					String combinedEncodes = encryptedTextInHex + encryptedAESinHex;
					System.out.print("Encrypted message and Key: " + combinedEncodes + "\n");


					
					byte[] attemptToDecrypt = hexToBytes(encryptedTextInHex);
					String decryptedText = null;
					try {
						decryptedText = decryptText(attemptToDecrypt, secKey);
					} catch (Exception e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					System.out.print("Decrypted message: ");
					System.out.print(decryptedText);
					System.out.print("\nTrying to append key to message before QR Code \n");
					System.out.print("Size of AES Key as string: " + secKeyString.length() + "\n\n");

					try {
						doBasicDemo(combinedEncodes);
						BufferedImage image = ImageIO.read(new File("QRCode.png"));
						QRImage.setIcon(new ImageIcon(image));

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		// set the size of the panel
		setSize(800, 800);
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

	private static byte[] hexToBytes(String hex) {
		return DatatypeConverter.parseHexBinary(hex);
	}
	
	public byte[] encryptAES(byte[] aesKey) throws Exception {

		Cipher cipher;

		byte[] cipherData = null;

		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

		cipher.init(Cipher.ENCRYPT_MODE, loadPublicKey());

		cipherData = cipher.doFinal(aesKey);

		return cipherData;

		}
	
	public void generateKeyPair(String publicFilePath, String privateFilePath) throws Exception {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

		keyGen.initialize(1024, random);

		KeyPair generatedKeyPair = keyGen.genKeyPair();

		savePublicKey(generatedKeyPair.getPublic(), publicFilePath);

		savePrivateKey(generatedKeyPair.getPrivate(), privateFilePath);

		}

		public void savePublicKey(PublicKey key, String filePath) throws Exception {

		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(

		key.getEncoded());

		FileOutputStream fos = new FileOutputStream(filePath);

		fos.write(x509EncodedKeySpec.getEncoded());

		fos.close();

		}

		private void savePrivateKey(PrivateKey key, String filePath) throws Exception {

		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(

		key.getEncoded());

		FileOutputStream fos = new FileOutputStream(filePath);

		fos.write(pkcs8EncodedKeySpec.getEncoded());

		fos.close();

		}
		

			private PublicKey loadPublicKey() throws Exception {

			File filePublicKey = new File(publicKeyFilePath);

			FileInputStream fis = new FileInputStream(filePublicKey);

			byte[] encodedPublicKey = new byte[fis.available()];

			fis.read(encodedPublicKey);

			fis.close();

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(

			encodedPublicKey);

			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

			return publicKey;

			}
			
			public byte[] encryptRSA(byte[] aesKey) throws Exception {

				Cipher cipher;

				byte[] cipherData = null;

				cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

				cipher.init(Cipher.ENCRYPT_MODE, loadPublicKey());

				cipherData = cipher.doFinal(aesKey);

				return cipherData;

				}
}
