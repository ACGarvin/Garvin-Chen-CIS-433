package io.nayuki.qrcodegen;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GeneratorFrame extends JFrame{

	private final JButton _GenerateButton = new JButton("Generate QR Code");
    protected JTextField textField = new JTextField(50);
    //File imgFile = new File("QRCode.png");
    
    public GeneratorFrame(){
		super("QR Generator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		JPanel textPanel = new JPanel(new FlowLayout());
		textPanel.add(textField);
		textPanel.add(_GenerateButton);
		
		add(textPanel, BorderLayout.SOUTH);
		JLabel QRImage = new JLabel();
		add(QRImage, BorderLayout.CENTER);
		
		_GenerateButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)  {
				String text = textField.getText();
				try {
					doBasicDemo(text);
					BufferedImage image = ImageIO.read(new File("QRCode.png"));
					QRImage.setIcon(new ImageIcon(image));
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		//set the size of the panel
		setSize(800, 500);
	}
    
    private static void doBasicDemo(String arg) throws IOException {
		//String text = arg;          // User-supplied Unicode text
		QrCode.Ecc errCorLvl = QrCode.Ecc.LOW;  // Error correction level
		
		QrCode qr = QrCode.encodeText(arg, errCorLvl);  // Make the QR Code symbol
		
		BufferedImage img = qr.toImage(10, 4);          // Convert to bitmap image
		File imgFile = new File("QRCode.png");  // File path for output
		ImageIO.write(img, "png", imgFile);             // Write image to file
		
		/*String svg = qr.toSvgString(4);  // Convert to SVG XML code
		try (Writer out = new OutputStreamWriter(
				new FileOutputStream("hello-world-QR.svg"),
				StandardCharsets.UTF_8)) {
			out.write(svg);  // Create/overwrite file and write SVG data
		}*/
	}
}

