package net.studioblueplanet.strava;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import net.miginfocom.swing.MigLayout;
import net.studioblueplanet.tomtomwatch.TomTomWatch;

public class SettingsDialog extends JDialog
{
	public enum PrefField
	{
		CLIENT_ID, SECRET, BACKUP_FOLDER, OPEN_WEATHER_API_KEY
	}

	private JPasswordField secretField;
	private JPasswordField clientIdField;
	private JPasswordField openWeatherField;
	private JTextField folderPathTextField;
	private static final Preferences prefs = Preferences.userRoot().node(TomTomWatch.class.getName());

	public SettingsDialog(Frame parent)
	{
		super(parent, "Password Dialog", true);
		setTitle("Settings");
		getContentPane().setLayout(new MigLayout("", "[166px][166px,grow][100px]", "[21px][21px][][21px][21px]"));

		// Create and add labels for the password fields
		JLabel secretLabel = new JLabel("Secret:");
		getContentPane().add(secretLabel, "cell 0 0,alignx right,growy");

		secretField = new JPasswordField(20);
		getContentPane().add(secretField, "cell 1 0,grow");

		// Create and add a button to show/hide password
		JButton showPasswordButton = new JButton("Show");
		showPasswordButton.addActionListener(new ActionListener()
			{
				boolean showPassword = false;

				@Override
				public void actionPerformed(ActionEvent e)
				{
					showPassword = !showPassword;
					if (showPassword)
					{
						secretField.setEchoChar((char) 0); // Show the password
						clientIdField.setEchoChar((char) 0); // Show the password
						openWeatherField.setEchoChar((char) 0); // Show the key
					}
					else
					{
						secretField.setEchoChar('*'); // Hide the password
						clientIdField.setEchoChar('*'); // Hide the password
						openWeatherField.setEchoChar('*'); // Hide the password
					}
				}
			});
		getContentPane().add(showPasswordButton, "cell 2 0 1 3,grow");

		JLabel clientIdLabel = new JLabel("Client ID:");
		getContentPane().add(clientIdLabel, "cell 0 1,alignx right,growy");

		clientIdField = new JPasswordField(20);
		getContentPane().add(clientIdField, "cell 1 1,grow");

		// Create and add a save button
		JButton saveButton = new JButton("Save & Close");
		saveButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					// Save the password values to System.getProperties()
					String secret = new String(secretField.getPassword());
					String clientId = new String(clientIdField.getPassword());
					String openweatherKey = new String(openWeatherField.getPassword());

					prefs.put(PrefField.SECRET.toString(), secret);
					prefs.put(PrefField.CLIENT_ID.toString(), clientId);
					prefs.put(PrefField.OPEN_WEATHER_API_KEY.toString(), openweatherKey);

					// Close the dialog
					dispose();
				}
			});

		JLabel openWeatherLabel = new JLabel("Open Weather API Key");
		getContentPane().add(openWeatherLabel, "cell 0 2,alignx trailing");

		openWeatherField = new JPasswordField(20);
		getContentPane().add(openWeatherField, "cell 1 2,grow");
		openWeatherField.setColumns(10);
		getContentPane().add(saveButton, "cell 2 4,grow");

		folderPathTextField = new JTextField();
		folderPathTextField.setEditable(false);
		getContentPane().add(folderPathTextField, "cell 1 3 2 1,grow");

		// Create and add a folder selection button
		JButton selectFolderButton = new JButton("Browse GPX Backup Folder");
		selectFolderButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					JFileChooser folderChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
					folderChooser.setDialogTitle("Select Folder");
					folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					int returnValue = folderChooser.showOpenDialog(null);
					if (returnValue == JFileChooser.APPROVE_OPTION)
					{
						File selectedFolder = folderChooser.getSelectedFile();
						String folderPath = selectedFolder.getAbsolutePath();
						folderPathTextField.setText(folderPath);
						// Save the selected folder path as a preference
						prefs.put(PrefField.BACKUP_FOLDER.toString(), folderPath);
					}
				}
			});
		getContentPane().add(selectFolderButton, "cell 0 3,grow");

		// Load saved folder path from preferences
		String savedFolderPath = prefs.get("selectedFolder", null);
		if (savedFolderPath != null)
		{
			folderPathTextField.setText(savedFolderPath);
		}

		// Load saved password values from System.getProperties()
		String savedSecret = prefs.get(PrefField.SECRET.toString(), null);
		String savedClientId = prefs.get(PrefField.CLIENT_ID.toString(), null);
		String savedOpenWeatherKey = prefs.get(PrefField.OPEN_WEATHER_API_KEY.toString(), null);

		if (savedSecret != null && savedClientId != null)
		{
			secretField.setText(savedSecret);
			clientIdField.setText(savedClientId);
			openWeatherField.setText(savedOpenWeatherKey);
		}

		pack();
		setLocationRelativeTo(parent);
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Password Dialog Test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JButton openDialogButton = new JButton("Open Password Dialog");
			openDialogButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						SettingsDialog dialog = new SettingsDialog(frame);
						dialog.setVisible(true);
					}
				});

			frame.add(openDialogButton);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}
