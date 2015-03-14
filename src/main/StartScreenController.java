package main;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import model.Friend;
import model.User;
import p2p.ClientHandler;
import p2p.Constants;
import remote.client.Client;
import server.Message;
import server.Server;
import server.Util;
import updater.Updater;
import utils.UtilDialogs;
import utils.UtilMusic;
import chat.Chat;

public class StartScreenController extends AnchorPane implements Initializable {

	@FXML
	private Button buttonFriendRequests;
	@FXML
	private Button buttonAddFriend;
	@FXML
	private TableColumn<Friend, String> nick;
	@FXML
	private TableColumn<Friend, ImageView> status;
	@FXML
	private TableView<Friend> table;
	@FXML
	private HBox searchBox;

	private preferences.Gui preferences;
	private SearchBox searchingBox;

	private static ObservableList<Friend> data = FXCollections.observableArrayList();
	private ObservableSet<String> blockList = FXCollections.observableSet();
	private ObservableSet<String> requestList = FXCollections.observableSet();

	ExecutorService executor = Executors.newSingleThreadExecutor();

	private Image imageOn = new Image("file:resources/on.png");
	private Image imageOff = new Image("file:resources/off.png");

	private Main application;

	public void setApp(Main application) {
		this.application = application;
	}

	public void initialize(URL location, ResourceBundle resources) {
		ImageView imageView = new ImageView(new Image("file:resources/friendrequest.png"));
		imageView.setFitWidth(40);
		imageView.setFitHeight(40);
		buttonFriendRequests.setGraphic(imageView);

		imageView = new ImageView(new Image("file:resources/search.png"));
		imageView.setFitWidth(40);
		imageView.setFitHeight(40);
		buttonAddFriend.setGraphic(imageView);

		initTable();
		autoRefreshing();

		String[] blocks = Server.getBlockList();
		for (String string : blocks) {
			blockList.add(string);
		}
		searchingBox = new SearchBox();
		searchBox.getChildren().add(searchingBox);
	}

	private void autoRefreshing() {
		Runnable updateOn = () -> {
			while (User.getUser().isLogged()) {
				WhoIsOnline();
				updateFriendRequests();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(updateOn).start();

	}

	private void initTable() {
		table.setPlaceholder(new Label("No friends"));
		nick.setCellValueFactory(cellData -> cellData.getValue().getNickProperty());
		status.setCellValueFactory(new PropertyValueFactory<Friend, ImageView>("image"));
		nick.setCellFactory(new Callback<TableColumn<Friend, String>, TableCell<Friend, String>>() {

			@Override
			public TableCell<Friend, String> call(TableColumn<Friend, String> col) {
				final TableCell<Friend, String> cell = new TableCell<>();
				cell.itemProperty().addListener(new ChangeListener<String>() {

					@Override
					public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
						if (newValue != null) {
							final ContextMenu cellMenu = new ContextMenu();
							final TableRow<?> row = cell.getTableRow();
							final ContextMenu rowMenu;
							if (row != null) {
								rowMenu = cell.getTableRow().getContextMenu();
								if (rowMenu != null) {
									cellMenu.getItems().addAll(rowMenu.getItems());
									cellMenu.getItems().add(new SeparatorMenuItem());
								} else {
									final ContextMenu tableMenu = cell.getTableView().getContextMenu();
									if (tableMenu != null) {
										cellMenu.getItems().addAll(tableMenu.getItems());
										cellMenu.getItems().add(new SeparatorMenuItem());
									}
								}
							}
							final MenuItem sendMessage = new MenuItem("Send Message");
							final MenuItem remoteControl = new MenuItem("Remote Control (broken)");
							final MenuItem deleteFriend = new MenuItem("Delete friend");
							final MenuItem block = new MenuItem("Block");
							final MenuItem unblock = new MenuItem("UnBlock");

							remoteControl.setOnAction((ActionEvent event) -> {
								startRemote((Friend) cell.getTableRow().getItem());
							});

							sendMessage.setOnAction((ActionEvent event) -> {
								startChat((Friend) cell.getTableRow().getItem());
							});

							deleteFriend.setOnAction((ActionEvent event) -> {
								deleteFriend((Friend) cell.getTableRow().getItem());
							});

							block.setOnAction((ActionEvent event) -> {
								addBlock(cell.getItem());
								cellMenu.getItems().remove(block);
								cellMenu.getItems().add(unblock);
							});
							unblock.setOnAction((ActionEvent event) -> {
								removeBlock(cell.getItem());
								cellMenu.getItems().remove(unblock);
								cellMenu.getItems().add(block);
							});

							cellMenu.getItems().add(sendMessage);
							cellMenu.getItems().add(remoteControl);
							cellMenu.getItems().add(deleteFriend);
							if (!blockList.contains(cell.getItem()))
								cellMenu.getItems().add(block);
							else
								cellMenu.getItems().add(unblock);

							cell.setContextMenu(cellMenu);
						} else {
							cell.setContextMenu(null);
						}
					}
				});
				cell.textProperty().bind(cell.itemProperty());
				return cell;
			}

		});

		table.setRowFactory(tv -> {
			TableRow<Friend> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Friend rowData = row.getItem();
					startChat(rowData);
				}
			});
			return row;
		});

		String[] friends = Server.getFriends();
		for (String string : friends) {
			data.add(new Friend(string, new ImageView(imageOff)));
		}

		table.setItems(data);

		table.getColumns().addListener(new ListChangeListener<Object>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onChanged(Change<?> change) {
				change.next();
				if (change.wasReplaced()) {
					table.getColumns().clear();
					table.getColumns().addAll(nick, status);
				}
			}
		});

	}

	public class SearchBox extends Region {

		public TextField textBox;
		public Button clearButton;

		public SearchBox() {
			setId("SearchBox");
			getStyleClass().add("search-box");
			setMinHeight(24);
			setPrefSize(200, 24);
			setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
			textBox = new TextField();
			textBox.setPromptText("Search new friend");
			clearButton = new Button();
			clearButton.setVisible(false);
			getChildren().addAll(textBox, clearButton);
			clearButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					textBox.setText("");
					textBox.requestFocus();
				}
			});
			textBox.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					clearButton.setVisible(textBox.getText().length() != 0);
					buttonAddFriend.setDisable(textBox.getText().length() == 0);
				}
			});
		}

		@Override
		protected void layoutChildren() {
			textBox.resize(getWidth(), getHeight());
			clearButton.resizeRelocate(getWidth() - 18, 6, 12, 13);
		}

		public String getText() {
			return this.textBox.getText();
		}
	}

	public void updateFriendRequests() {
		new Thread(() -> {
			String[] requests = Server.getRequests();
			if (requests.length > 0) {
				if (requests.length > requestList.size())
					UtilMusic.play(UtilMusic.newFriendRequest);

				Platform.runLater(() -> {
					buttonFriendRequests.setText(String.valueOf(requests.length));
					buttonFriendRequests.setDisable(false);
				});
				for (String string : requests) {
					requestList.add(string);
				}
			} else {
				Platform.runLater(() -> {
					buttonFriendRequests.setText("");
				});
				buttonFriendRequests.setDisable(true);
			}
		}).start();

	}

	public void addBlock(String block) {
		new Thread(() -> {
			Server.addBlock(block);
		}).start();
		blockList.add(block);
	}

	public void removeBlock(String unblock) {
		new Thread(() -> {
			Server.removeBlock(unblock);
		}).start();
		blockList.remove(unblock);
	}

	private void addFriend(String friend) {
		new Thread(() -> {
			if (Server.addFriend(friend)) {
				data.add(new Friend(friend, new ImageView(new Image("file:resources/off.png"))));
				WhoIsOnline();
			} else {
				Platform.runLater(() -> {
					UtilDialogs.error(1234);
				});
			}

		}).start();
	}

	public void deleteFriend(Friend friend) {
		Server.deleteFriend(friend.getNick());
		data.remove(friend);
	}

	@FXML
	public void searchFriend(ActionEvent event) {
		new Thread(() -> {
			String friendToAdd = searchingBox.getText();

			for (Friend friend : data) {
				if (friend.getNick().equals(friendToAdd)) {
					UtilDialogs.friendAlreadyExists();
					return;
				}
			}

			if (Server.searchFriend(friendToAdd)) {
				Platform.runLater(() -> {
					if (UtilDialogs.addFriend(friendToAdd))
						addFriend(friendToAdd);
				});
			} else {
				UtilDialogs.userDoesntExists();
			}
		}).start();

	}

	private void WhoIsOnline() {
		new Thread(() -> {
			String[] onlines = Server.whoIsOnline();

			for (int i = 0; i < data.size(); i++) {
				Friend friend = data.get(i);
				if (friend.getImage().getImage().equals(Chat.imageUnread))
					continue;
				friend.setImage(imageOff);
				for (int j = 0; j < onlines.length; j++) {
					if (friend.getNick().equals(onlines[j])) {
						friend.setImage(imageOn);
					}
				}
			}
		}).start();
	}

	private boolean startConnectFriend(Friend friend) {
		if (!friend.isConnected()) {
			String[] address = Server.getIp(friend.getNick());
			if (address.length == 0) {
				UtilDialogs
						.information("Cant reach friend, there are 3 possible reasons \n 1) Friend offline. \n 2) You in his block list \n "
								+ "3) His Chat port blocked by firewall or router");
				return false;
			} else {
				if (friend.startConnection(address[0], address[1])) {
					new ClientHandler(friend).start();
					return true;
				} else {
					UtilDialogs
							.information("Cant reach friend, there are 3 possible reasons \n 1) Friend offline. \n 2) You in his block list \n "
									+ "3) His Chat port blocked by firewall or router");
					return false;
				}
			}
		}
		return true;
	}

	private void startChat(Friend friend) {
		new Thread(() -> {
			synchronized (Chat.lockMap) {
				if (Chat.chats.containsKey(friend.getNick())) {
					Chat.chats.get(friend.getNick()).show();
				} else {
					if (startConnectFriend(friend)) {
						Chat chat = new Chat(friend);
						chat.show();
						Chat.chats.put(friend.getNick(), chat);
					}
				}
				if (friend.getImage().getImage().equals(Chat.imageUnread)) {
					friend.setImage(imageOn);
					WhoIsOnline();
				}
			}
		}).start();
	}

	private void startRemote(Friend friend) {
		if (!startConnectFriend(friend))
			return;

		new Thread(new Runnable() {

			@Override
			public void run() {
				new Thread(new Runnable() {

					@Override
					public void run() {
						Client.startRmote(5557);
					}
				}).start();

				Message message = new Message();
				message.setMessage(Constants.REMOTE, User.getUser().getIp());

				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Util.sendBytes(message.toString(), friend.getOutput());
			}
		}).start();
	}

	public static ObservableList<Friend> getFriends() {
		return data;
	}

	public void logout(ActionEvent event) {
		User.getUser().setLogged(false);
		Server.logout();
		data.clear();
		blockList.clear();
		application.gotoLogin();

	}

	@FXML
	public void close(ActionEvent event) {
		Platform.exit();
		System.exit(0);
	}

	@FXML
	public void about(ActionEvent event) {
		UtilDialogs.about();
	}

	@FXML
	public void friendRequests(ActionEvent event) {
		String friend = UtilDialogs.friendRequests(requestList);
		if (friend != null) {
			addFriend(friend);
			updateFriendRequests();
		}
	}

	@FXML
	public void checkUpdates(ActionEvent event) {
		Updater.checkUpdate();
	}

	@FXML
	public void blockList(ActionEvent event) {
		// TODO:
		UtilDialogs.information("Under Construction");

	}

	@FXML
	public void properties(ActionEvent event) {
		preferences = new preferences.Gui();
		preferences.show();
	}

}
