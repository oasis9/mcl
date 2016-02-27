package net.minecraft;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.VolatileImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.imageio.ImageIO;

import java.awt.GridBagConstraints;
import java.awt.FlowLayout;

public class LoginForm extends Panel {

	private static final long serialVersionUID = 1L;

	private Image bgImage;
	public static LoginForm lf;
	private TextField userNameBox = new TextField("", 20);
	private Checkbox forceUpdateBox = new Checkbox("Обновить клиент!");
	private Button launchButton = new Button("Войти в игру");
	private Label errorLabel = new Label("", 1);
	private Choice versionChoice = new Choice();
	private LauncherFrame launcherFrame;
	private boolean outdated = false;
	private VolatileImage img;
	private Button optionsButton = new Button("Настройки");
	private Label versionLabel = new Label("Версия:");
	private Label login = new Label("Логин:", 2);
	private boolean empty = true;

	public LoginForm(LauncherFrame launcherFrame) {
		SettingsFrame.langChoice.add("Russian");
		SettingsFrame.langChoice.add("English");
		this.launcherFrame = launcherFrame;
		this.lf = this;
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridx = 0;
		add(buildLoginPanel(), gbc_panel);
		try {
			this.bgImage = ImageIO
					.read(LoginForm.class.getResource("dirt.png"))
					.getScaledInstance(32, 32, 16);
		} catch (IOException e) {
			e.printStackTrace();
		}

		readUsername();

		this.launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (userNameBox.getText().equalsIgnoreCase("")) {
					/*
					 * JOptionPane.showMessageDialog( null,
					 * "Логин отсутствует! Введите его и повторите вход" , "",
					 * JOptionPane.ERROR_MESSAGE ); return;
					 */
					userNameBox.setText("player");
				}

				if (LoginForm.this.forceUpdateBox.getState()) {
					LoginForm.this.launcherFrame.forceUpdate = true;
				}
				loginOk();
				LoginForm.this.launcherFrame.login(
						LoginForm.this.userNameBox.getText(),
						LoginForm.this.versionChoice.getSelectedItem());
			}
		});
		loadLangLauncher();

	}

	public void loadLangLauncher() {
		Properties lang = new Properties();
		InputStream file = null;
		try {

			Settings settings = SettingsLoader.load();
			try {
				file = LoginForm.class
						.getResourceAsStream("/net/minecraft/lang/"
								+ settings.lang + ".properties");
				if(file == null)
					throw new NullPointerException();
			} catch (NullPointerException e) {
				file = LoginForm.class
						.getResourceAsStream("/net/minecraft/lang/ru_RU.properties");
			}
			lang.load(file);
			this.forceUpdateBox.setLabel((String) lang.get("forceUpdateBox"));
			this.launchButton.setLabel((String) lang.get("launchButton"));
			this.login.setText((String) lang.get("login"));
			this.optionsButton.setLabel((String) lang.get("optionsButton"));
			this.versionLabel.setText((String) lang.get("versionLabel"));
			if (this.empty) {
				this.versionLabel.setText((String) lang.get("emptyversion"));
			}

			SettingsFrame.buttonexit.setText((String) lang.get("buttonexit"));
			SettingsFrame.buttonsave.setText((String) lang.get("buttonsave"));
			SettingsFrame.labelChangeLang.setText((String) lang
					.get("labelChangeLang"));
			SettingsFrame.langChoice.select(Integer.valueOf((String) lang
					.get("lang")));
			SettingsFrame.Title = (String) lang.get("TitleSettings");
			SettingsFrame.memory = (String)lang.get("memory");
			SettingsFrame.min = (String)lang.get("min");
			SettingsFrame.max = (String)lang.get("max");
			SettingsFrame.session = (String)lang.get("session");
			
			file.close();

			launcherFrame.pack();
			launcherFrame.repaint();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void readUsername() {
		try {
			File lastLogin = new File(MinecraftUtil.getWorkingDirectory(),
					"lastlogin");

			Cipher cipher = getCipher(2, "passwordfile");
			DataInputStream dis;

			if (cipher != null)
				dis = new DataInputStream(new CipherInputStream(
						new FileInputStream(lastLogin), cipher));
			else {
				dis = new DataInputStream(new FileInputStream(lastLogin));
			}
			this.userNameBox.setText(dis.readUTF());
			dis.close();
		} catch (Exception e) {
		}
	}

	private void writeUsername() {
		try {
			File lastLogin = new File(MinecraftUtil.getWorkingDirectory(),
					"lastlogin");

			Cipher cipher = getCipher(1, "passwordfile");
			DataOutputStream dos;

			if (cipher != null)
				dos = new DataOutputStream(new CipherOutputStream(
						new FileOutputStream(lastLogin), cipher));
			else {
				dos = new DataOutputStream(new FileOutputStream(lastLogin));
			}

			dos.writeUTF(this.userNameBox.getText());
			dos.writeUTF("");
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Cipher getCipher(int mode, String password) throws Exception {
		Random random = new Random(43287234L);
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

		SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
				.generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(mode, pbeKey, pbeParamSpec);
		return cipher;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g2) {
		int w = getWidth() / 2;
		int h = getHeight() / 2;

		if ((this.img == null) || (this.img.getWidth() != w)
				|| (this.img.getHeight() != h)) {
			this.img = createVolatileImage(w, h);
		}

		Graphics g = this.img.getGraphics();

		for (int x = 0; x <= w / 32; x++) {
			for (int y = 0; y <= h / 32; y++) {
				g.drawImage(this.bgImage, x * 32, y * 32, null);
			}
		}

		g.setColor(Color.LIGHT_GRAY);

		String msg = "Minecraft Launcher";
		g.setFont(new Font(null, 1, 20));
		FontMetrics fm = g.getFontMetrics();
		g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2,
				h / 2 - fm.getHeight() * 2);

		g.dispose();
		g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);
	}

	private Panel buildLoginPanel() {
		Panel panel = new Panel() {

			private static final long serialVersionUID = 1L;

			private Insets insets = new Insets(12, 24, 16, 32);

			public Insets getInsets() {
				return this.insets;
			}

			public void update(Graphics g) {
				paint(g);
			}

			public void paint(Graphics g) {
				super.paint(g);

				int hOffs = 0;

				g.setColor(Color.BLACK);
				g.drawRect(0, 0 + hOffs, getWidth() - 1, getHeight() - 1
						- hOffs);
				g.drawRect(1, 1 + hOffs, getWidth() - 3, getHeight() - 3
						- hOffs);
				g.setColor(Color.WHITE);

				g.drawRect(2, 2 + hOffs, getWidth() - 5, getHeight() - 5
						- hOffs);
			}
		};

		panel.setBackground(Color.GRAY);
		BorderLayout layout = new BorderLayout();
		layout.setHgap(0);
		layout.setVgap(8);
		panel.setLayout(layout);

		GridLayout gl1 = new GridLayout(0, 1);
		GridLayout gl2 = new GridLayout(0, 1);
		gl1.setVgap(2);
		gl2.setVgap(2);
		Panel titles = new Panel(gl1);
		Panel values = new Panel(gl2);

		titles.add(login);
		titles.add(new Label("", 2));
		values.add(this.userNameBox);
		forceUpdateBox.setEnabled(false);
		values.add(this.forceUpdateBox);

		panel.add(titles, "West");
		panel.add(values, "Center");

		Panel loginPanel = new Panel(new BorderLayout());

		Panel registerPanel = new Panel(new BorderLayout());

		try {
			if (this.outdated) {
				Label accountLink = new Label("Обновите лаунчер!") {
					private static final long serialVersionUID = 0L;

					public void paint(Graphics g) {
						super.paint(g);

						int x = 0;
						int y = 0;

						FontMetrics fm = g.getFontMetrics();

						int width = fm.stringWidth(getText());
						int height = fm.getHeight();

						if (getAlignment() == 0)
							x = 0;
						else if (getAlignment() == 1)
							x = getBounds().width / 2 - width / 2;
						else if (getAlignment() == 2) {
							x = getBounds().width - width;
						}
						y = getBounds().height / 2 + height / 2 - 1;

						g.drawLine(x + 2, y, x + width - 2, y);
					}

					public void update(Graphics g) {
						paint(g);
					}
				};

				accountLink.setCursor(Cursor.getPredefinedCursor(12));
				accountLink.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent arg0) {
						try {
							Desktop.getDesktop()
									.browse(new URL(
											"http://www.minecraft.net/download.jsp")
											.toURI());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				accountLink.setForeground(Color.BLUE);
				registerPanel.add(accountLink, "West");
				registerPanel.add(new Panel(), "Center");
			}
		} catch (Error localError) {
		}

		loginPanel.add(registerPanel, "Center");

		optionsButton.setEnabled(true);
		optionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				new SettingsFrame();
			}
		});
		registerPanel.add(optionsButton, BorderLayout.WEST);
		loginPanel.add(this.launchButton, "East");
		try {
			for (File version : new File(MinecraftUtil.getWorkingDirectory(),
					"versions").listFiles()) {
				if (version.isDirectory()) {
					for (String name : version.list()) {
						if (name.equals(version.getName() + ".json")) {
							versionChoice.add(version.getName());
							this.empty = false;
						}
					}
				}
			}
		} catch (NullPointerException e) {

		}

		if (this.empty) {
			launchButton.setEnabled(false);
			versionLabel.setText("Список установленных версий пуст");
		}

		Panel anjoPanel = new Panel();
		loginPanel.add(anjoPanel, "South");
		anjoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		anjoPanel.add(versionLabel);
		if (!empty)
			anjoPanel.add(versionChoice);

		panel.add(loginPanel, "South");

		this.errorLabel.setFont(new Font(null, 2, 16));
		this.errorLabel.setForeground(new Color(8388608));

		panel.add(this.errorLabel, "North");

		return panel;
	}

	public void setError(String errorMessage) {
		removeAll();
		add(buildLoginPanel());

		this.errorLabel.setText(errorMessage);
		validate();
	}

	public void loginOk() {
		writeUsername();
	}
}