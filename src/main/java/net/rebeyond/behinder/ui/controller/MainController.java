//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.rebeyond.behinder.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.net.*;
import java.net.Proxy.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainController {
    public static Map<String, Object> currentProxy = new HashMap<>();
    @FXML
    private TreeView treeview;
    @FXML
    private TableView<List<SimpleStringProperty>> shellListTable;
    @FXML
    private TableColumn<List<SimpleStringProperty>, String> urlCol;
    @FXML
    private TableColumn<List<SimpleStringProperty>, String> ipCol;
    @FXML
    private TableColumn<List<SimpleStringProperty>, String> typeCol;
    @FXML
    private TableColumn<List<SimpleStringProperty>, String> osCol;
    @FXML
    private TableColumn<List<SimpleStringProperty>, String> commentCol;
    @FXML
    private TableColumn<List<SimpleStringProperty>, String> addTimeCol;
    @FXML
    private MenuItem proxySetupBtn;
    @FXML
    private Label statusLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private Label proxyStatusLabel;
    @FXML
    private TreeView<String> categoryTreeView;
    private ShellManager shellManager;

    public MainController() {
        try {
            this.shellManager = new ShellManager();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            this.showErrorMessage("错误", "数据库文件丢失");
            System.exit(0);
        }

    }

    public void initialize() {
        try {
            this.initCatagoryList();
            this.initShellList();
            this.initToolbar();
            this.initBottomBar();
            this.loadProxy();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initBottomBar() {
        this.versionLabel.setText(String.format(this.versionLabel.getText(), Constants.VERSION));
    }

    private void loadProxy() throws Exception {
        JSONObject proxyObj = this.shellManager.findProxy("default");
        int status = proxyObj.getInt("status");
        String type = proxyObj.getString("type");
        String ip = proxyObj.getString("ip");
        String port = proxyObj.get("port").toString();
        String username = proxyObj.getString("username");
        String password = proxyObj.getString("password");
        if (status == Constants.PROXY_ENABLE) {
            currentProxy.put("username", username);
            currentProxy.put("password", password);
            InetSocketAddress proxyAddr = new InetSocketAddress(ip, Integer.parseInt(port));
            Proxy proxy;
            if (type.equals("HTTP")) {
                proxy = new Proxy(Type.HTTP, proxyAddr);
                currentProxy.put("proxy", proxy);
            } else if (type.equals("SOCKS")) {
                proxy = new Proxy(Type.SOCKS, proxyAddr);
                currentProxy.put("proxy", proxy);
            }

            this.proxyStatusLabel.setText("代理生效中");
        }

    }

    private void initToolbar() {
        this.proxySetupBtn.setOnAction((event) -> {
            Alert inputDialog = new Alert(AlertType.NONE);
            Window window = inputDialog.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest((e) -> {
                window.hide();
            });
            ToggleGroup statusGroup = new ToggleGroup();
            RadioButton enableRadio = new RadioButton("启用");
            RadioButton disableRadio = new RadioButton("禁用");
            enableRadio.setToggleGroup(statusGroup);
            disableRadio.setToggleGroup(statusGroup);
            HBox statusHbox = new HBox();
            statusHbox.setSpacing(10.0D);
            statusHbox.getChildren().add(enableRadio);
            statusHbox.getChildren().add(disableRadio);
            GridPane proxyGridPane = new GridPane();
            proxyGridPane.setVgap(15.0D);
            proxyGridPane.setPadding(new Insets(20.0D, 20.0D, 0.0D, 10.0D));
            Label typeLabel = new Label("类型：");
            ComboBox<String> typeCombo = new ComboBox<>();
            typeCombo.setItems(FXCollections.observableArrayList("HTTP", "SOCKS"));
            typeCombo.getSelectionModel().select(0);
            Label IPLabel = new Label("IP地址：");
            TextField IPText = new TextField();
            Label PortLabel = new Label("端口：");
            TextField PortText = new TextField();
            Label userNameLabel = new Label("用户名：");
            TextField userNameText = new TextField();
            Label passwordLabel = new Label("密码：");
            TextField passwordText = new TextField();
            Button cancelBtn = new Button("取消");
            Button saveBtn = new Button("保存");

            try {
                JSONObject proxyObj = this.shellManager.findProxy("default");
                if (proxyObj != null) {
                    int status = proxyObj.getInt("status");
                    if (status == Constants.PROXY_ENABLE) {
                        enableRadio.setSelected(true);
                    } else if (status == Constants.PROXY_DISABLE) {
                        disableRadio.setSelected(true);
                    }

                    String type = proxyObj.getString("type");
                    if (type.equals("HTTP")) {
                        typeCombo.getSelectionModel().select(0);
                    } else if (type.equals("SOCKS")) {
                        typeCombo.getSelectionModel().select(1);
                    }

                    String ip = proxyObj.getString("ip");
                    String port = proxyObj.get("port").toString();
                    IPText.setText(ip);
                    PortText.setText(port);
                    String username = proxyObj.getString("username");
                    String password = proxyObj.getString("password");
                    userNameText.setText(username);
                    passwordText.setText(password);
                }
            } catch (Exception var28) {
                this.statusLabel.setText("代理服务器配置加载失败。");
                var28.printStackTrace();
            }

            saveBtn.setOnAction((e) -> {
                if (disableRadio.isSelected()) {
                    currentProxy.put("proxy", null);
                    this.proxyStatusLabel.setText("");

                    try {
                        this.shellManager.updateProxy("default", typeCombo.getSelectionModel().getSelectedItem(), IPText.getText(), PortText.getText(), userNameText.getText(), passwordText.getText(), Constants.PROXY_DISABLE);
                    } catch (Exception var12) {
                        var12.printStackTrace();
                    }

                    inputDialog.getDialogPane().getScene().getWindow().hide();
                } else {
                    try {
                        this.shellManager.updateProxy("default", typeCombo.getSelectionModel().getSelectedItem(), IPText.getText(), PortText.getText(), userNameText.getText(), passwordText.getText(), Constants.PROXY_ENABLE);
                    } catch (Exception var13) {
                        var13.printStackTrace();
                    }

                    String type;
                    if (!userNameText.getText().trim().equals("")) {
                        final String proxyUser = userNameText.getText().trim();
                        final String proxyPass = passwordText.getText();
                        Authenticator.setDefault(new Authenticator() {
                            public PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
                            }
                        });
                    } else {
                        Authenticator.setDefault(null);
                    }

                    currentProxy.put("username", userNameText.getText());
                    currentProxy.put("password", passwordText.getText());
                    InetSocketAddress proxyAddr = new InetSocketAddress(IPText.getText(), Integer.parseInt(PortText.getText()));
                    type = typeCombo.getValue();
                    Proxy proxy;
                    if (type.equals("HTTP")) {
                        proxy = new Proxy(Type.HTTP, proxyAddr);
                        currentProxy.put("proxy", proxy);
                    } else if (type.equals("SOCKS")) {
                        proxy = new Proxy(Type.SOCKS, proxyAddr);
                        currentProxy.put("proxy", proxy);
                    }

                    this.proxyStatusLabel.setText("代理生效中");
                    inputDialog.getDialogPane().getScene().getWindow().hide();
                }
            });
            cancelBtn.setOnAction((e) -> {
                inputDialog.getDialogPane().getScene().getWindow().hide();
            });
            proxyGridPane.add(statusHbox, 1, 0);
            proxyGridPane.add(typeLabel, 0, 1);
            proxyGridPane.add(typeCombo, 1, 1);
            proxyGridPane.add(IPLabel, 0, 2);
            proxyGridPane.add(IPText, 1, 2);
            proxyGridPane.add(PortLabel, 0, 3);
            proxyGridPane.add(PortText, 1, 3);
            proxyGridPane.add(userNameLabel, 0, 4);
            proxyGridPane.add(userNameText, 1, 4);
            proxyGridPane.add(passwordLabel, 0, 5);
            proxyGridPane.add(passwordText, 1, 5);
            HBox buttonBox = new HBox();
            buttonBox.setSpacing(20.0D);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().add(cancelBtn);
            buttonBox.getChildren().add(saveBtn);
            GridPane.setColumnSpan(buttonBox, 2);
            proxyGridPane.add(buttonBox, 0, 6);
            inputDialog.getDialogPane().setContent(proxyGridPane);
            inputDialog.showAndWait();
        });
    }

    private void initCatagoryList() throws Exception {
        this.initCatagoryTree();
        this.initCatagoryMenu();
    }

    private void initShellList() throws Exception {
        this.initShellTable();
        this.loadShellList();
        this.loadContextMenu();
    }

    private void initShellTable() throws Exception {
        ObservableList<TableColumn<List<SimpleStringProperty>, ?>> tcs = this.shellListTable.getColumns();

        for (int i = 0; i < tcs.size(); ++i) {
            int finalI = i;
            ((TableColumn<List<SimpleStringProperty>, String>) tcs.get(i)).setCellValueFactory((data) -> ((List<SimpleStringProperty>) data.getValue()).get(finalI));
        }

        this.shellListTable.setRowFactory((tv) -> {
            TableRow<List<SimpleStringProperty>> row = new TableRow<>();
            row.setOnMouseClicked((event) -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    String url = row.getItem().get(0).getValue();
                    String shellID = row.getItem().get(6).getValue();
                    this.openShell(url, shellID);
                }

            });
            return row;
        });
    }

    private boolean checkUrl(String urlString) {
        try {
            new URL(urlString.trim());
            return true;
        } catch (Exception var4) {
            this.showErrorMessage("错误", "URL格式错误");
            return false;
        }
    }

    private boolean checkPassword(String password) {
        if (password.length() > 255) {
            this.showErrorMessage("错误", "密码长度不应大于255个字符");
            return false;
        } else if (password.length() < 1) {
            this.showErrorMessage("错误", "密码不能为空，请输入密码");
            return false;
        } else {
            return true;
        }
    }

    private void showShellDialog(int shellID) throws Exception {
        Alert alert = new Alert(AlertType.NONE);
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest((e) -> {
            window.hide();
        });
        alert.setTitle("新增Shell");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("res/net/rebeyond/resource/logo.jpg"))));
        alert.setHeaderText("");
        TextField urlText = new TextField();
        TextField passText = new TextField();
        ComboBox<String> shellType = new ComboBox<>();
        ObservableList<String> typeList = FXCollections.observableArrayList("jsp", "php", "aspx", "asp");
        shellType.setItems(typeList);
        ComboBox<String> shellCatagory = new ComboBox<>();

        try {
            JSONArray categoryArr = this.shellManager.listCatagory();
            ObservableList<String> categoryList = FXCollections.observableArrayList();

            for (int i = 0; i < categoryArr.length(); ++i) {
                JSONObject categoryObj = categoryArr.getJSONObject(i);
                categoryList.add(categoryObj.getString("name"));
            }

            shellCatagory.setItems(categoryList);
            shellCatagory.getSelectionModel().select(0);
        } catch (Exception var16) {
            var16.printStackTrace();
        }

        TextArea header = new TextArea();
        TextArea commnet = new TextArea();
        urlText.textProperty().addListener((observable, oldValue, newValue) -> {
            URL url;
            try {
                url = new URL(urlText.getText().trim());
            } catch (Exception var8) {
                return;
            }

            String extension = url.getPath().substring(url.getPath().lastIndexOf(".") + 1).toLowerCase();

            for (int i = 0; i < shellType.getItems().size(); ++i) {
                if (extension.toLowerCase().equals(shellType.getItems().get(i))) {
                    shellType.getSelectionModel().select(i);
                }
            }

        });
        Button saveBtn = new Button("保存");
        Button cancelBtn = new Button("取消");
        GridPane vpsInfoPane = new GridPane();
        GridPane.setMargin(vpsInfoPane, new Insets(20.0D, 0.0D, 0.0D, 0.0D));
        vpsInfoPane.setVgap(10.0D);
        vpsInfoPane.setMaxWidth(1.7976931348623157E308D);
        vpsInfoPane.add(new Label("URL："), 0, 0);
        vpsInfoPane.add(urlText, 1, 0);
        vpsInfoPane.add(new Label("密码："), 0, 1);
        vpsInfoPane.add(passText, 1, 1);
        vpsInfoPane.add(new Label("脚本类型："), 0, 2);
        vpsInfoPane.add(shellType, 1, 2);
        vpsInfoPane.add(new Label("分类："), 0, 3);
        vpsInfoPane.add(shellCatagory, 1, 3);
        vpsInfoPane.add(new Label("自定义请求头："), 0, 4);
        vpsInfoPane.add(header, 1, 4);
        vpsInfoPane.add(new Label("备注："), 0, 5);
        vpsInfoPane.add(commnet, 1, 5);
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(20.0D);
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        vpsInfoPane.add(buttonBox, 0, 8);
        GridPane.setColumnSpan(buttonBox, 2);
        alert.getDialogPane().setContent(vpsInfoPane);
        if (shellID != -1) {
            JSONObject shellObj = this.shellManager.findShell(shellID);
            urlText.setText(shellObj.getString("url"));
            passText.setText(shellObj.getString("password"));
            shellType.setValue(shellObj.getString("type"));
            shellCatagory.setValue(shellObj.getString("category"));
            header.setText(shellObj.getString("headers"));
            commnet.setText(shellObj.getString("comment"));
        }

        saveBtn.setOnAction((e) -> {
            String url = urlText.getText().trim();
            String password = passText.getText();
            if (this.checkUrl(url) && this.checkPassword(password)) {
                String type = shellType.getValue();
                String category = shellCatagory.getValue();
                String comment = commnet.getText();
                String headers = header.getText();

                try {
                    if (shellID == -1) {
                        this.shellManager.addShell(url, password, type, category, comment, headers);
                    } else {
                        this.shellManager.updateShell(shellID, url, password, type, category, comment, headers);
                    }

                    this.loadShellList();
                    return;
                } catch (Exception var20) {
                    var20.printStackTrace();
                    this.showErrorMessage("保存失败", var20.getMessage());
                } finally {
                    alert.getDialogPane().getScene().getWindow().hide();
                }

            }
        });
        cancelBtn.setOnAction((e) -> {
            alert.getDialogPane().getScene().getWindow().hide();
        });
        alert.showAndWait();
    }

    private void openShell(String url, String shellID) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/net/rebeyond/behinder/ui/MainWindow.fxml"));
            Parent mainWindow = loader.load();
            MainWindowController mainWindowController = loader.getController();
            mainWindowController.init(this.shellManager.findShell(Integer.parseInt(shellID)), this.shellManager, currentProxy);
            Stage stage = new Stage();
            stage.setTitle(url);
            stage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("res/net/rebeyond/resource/logo.jpg"))));
            stage.setUserData(url);
            stage.setScene(new Scene(mainWindow));
            stage.setOnCloseRequest((e) -> {

                for (Thread o : mainWindowController.getWorkList()) {
                    Thread worker = o;
                    worker.interrupt();
                }

            });
            stage.show();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    private void loadContextMenu() {
        ContextMenu cm = new ContextMenu();
        MenuItem openBtn = new MenuItem("打开");
        cm.getItems().add(openBtn);
        MenuItem addBtn = new MenuItem("新增");
        cm.getItems().add(addBtn);
        MenuItem editBtn = new MenuItem("编辑");
        cm.getItems().add(editBtn);
        MenuItem delBtn = new MenuItem("删除");
        cm.getItems().add(delBtn);
        MenuItem copyBtn = new MenuItem("复制URL");
        cm.getItems().add(copyBtn);
        SeparatorMenuItem separatorBtn = new SeparatorMenuItem();
        cm.getItems().add(separatorBtn);
        MenuItem refreshBtn = new MenuItem("刷新");
        cm.getItems().add(refreshBtn);
        this.shellListTable.setContextMenu(cm);
        openBtn.setOnAction((event) -> {
            String url = ((StringProperty) ((List) this.shellListTable.getSelectionModel().getSelectedItem()).get(0)).getValue();
            String shellID = ((StringProperty) ((List) this.shellListTable.getSelectionModel().getSelectedItem()).get(6)).getValue();

            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/net/rebeyond/behinder/ui/MainWindow.fxml"));
                Parent mainWindow = loader.load();
                MainWindowController mainWindowController = loader.getController();
                mainWindowController.init(this.shellManager.findShell(Integer.parseInt(shellID)), this.shellManager, currentProxy);
                Stage stage = new Stage();
                stage.setTitle(url);
                stage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("res/net/rebeyond/resource/logo.jpg"))));
                stage.setUserData(url);
                stage.setScene(new Scene(mainWindow));
                stage.setOnCloseRequest((e) -> {

                    for (Thread o : mainWindowController.getWorkList()) {
                        Thread worker = o;
                        worker.interrupt();
                    }

                });
                stage.show();
            } catch (Exception var8) {
                var8.printStackTrace();
            }

        });
        addBtn.setOnAction((event) -> {
            try {
                this.showShellDialog(-1);
            } catch (Exception var3) {
                this.showErrorMessage("错误", "新增失败：" + var3.getMessage());
                var3.printStackTrace();
            }

        });
        editBtn.setOnAction((event) -> {
            String shellID = ((StringProperty) ((List) this.shellListTable.getSelectionModel().getSelectedItem()).get(6)).getValue();

            try {
                this.showShellDialog(Integer.parseInt(shellID));
            } catch (Exception var4) {
                this.showErrorMessage("错误", "编辑失败：" + var4.getMessage());
                var4.printStackTrace();
            }

        });
        delBtn.setOnAction((event) -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setHeaderText("");
            alert.setContentText("请确认是否删除？");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                String shellID = ((StringProperty) ((List) this.shellListTable.getSelectionModel().getSelectedItem()).get(6)).getValue();

                try {
                    this.shellManager.deleteShell(Integer.parseInt(shellID));
                    this.loadShellList();
                } catch (Exception var6) {
                    var6.printStackTrace();
                }
            }

        });
        copyBtn.setOnAction((event) -> {
            String url = ((StringProperty) ((List) this.shellListTable.getSelectionModel().getSelectedItem()).get(0)).getValue();
            this.copyString(url);
        });
        refreshBtn.setOnAction((event) -> {
            try {
                this.loadShellList();
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        });
    }

    private void loadShellList() throws Exception {
        this.shellListTable.getItems().clear();
        JSONArray shellList = this.shellManager.listShell();
        this.fillShellRows(shellList);
    }

    private void fillShellRows(JSONArray jsonArray) {
        ObservableList<List<SimpleStringProperty>> data = FXCollections.observableArrayList();

        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject rowObj = jsonArray.getJSONObject(i);

            try {
                int id = rowObj.getInt("id");
                String url = rowObj.getString("url");
                String ip = rowObj.getString("ip");
                String type = rowObj.getString("type");
                String os = rowObj.getString("os");
                String comment = rowObj.getString("comment");
                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String addTime = df.format(new Timestamp(rowObj.getLong("addtime")));
                List<SimpleStringProperty> row = new ArrayList<>();
                row.add(0, new SimpleStringProperty(url));
                row.add(1, new SimpleStringProperty(ip));
                row.add(2, new SimpleStringProperty(type));
                row.add(3, new SimpleStringProperty(os));
                row.add(4, new SimpleStringProperty(comment));
                row.add(5, new SimpleStringProperty(addTime));
                row.add(6, new SimpleStringProperty(id + ""));
                data.add(row);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.shellListTable.setItems(data);
    }

    private void copyString(String str) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(str);
        clipboard.setContent(content);
    }

    private void showErrorMessage(String title, String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest((event) -> {
            window.hide();
        });
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(msg);
        alert.show();
    }

    private void initCatagoryMenu() {
        ContextMenu treeContextMenu = new ContextMenu();
        MenuItem addCatagoryBtn = new MenuItem("新增");
        treeContextMenu.getItems().add(addCatagoryBtn);
        MenuItem delCatagoryBtn = new MenuItem("删除");
        treeContextMenu.getItems().add(delCatagoryBtn);
        addCatagoryBtn.setOnAction((event) -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("新增分类");
            alert.setHeaderText("");
            GridPane panel = new GridPane();
            Label cataGoryNameLable = new Label("请输入分类名称：");
            TextField cataGoryNameTxt = new TextField();
            Label cataGoryCommentLable = new Label("请输入分类描述：");
            TextField cataGoryCommentTxt = new TextField();
            panel.add(cataGoryNameLable, 0, 0);
            panel.add(cataGoryNameTxt, 1, 0);
            panel.add(cataGoryCommentLable, 0, 1);
            panel.add(cataGoryCommentTxt, 1, 1);
            panel.setVgap(20.0D);
            alert.getDialogPane().setContent(panel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                    if (this.shellManager.addCatagory(cataGoryNameTxt.getText(), cataGoryCommentTxt.getText()) > 0) {
                        this.statusLabel.setText("分类新增完成");
                        this.initCatagoryTree();
                    }
                } catch (Exception var10) {
                    this.statusLabel.setText("分类新增失败：" + var10.getMessage());
                    var10.printStackTrace();
                }
            }

        });
        delCatagoryBtn.setOnAction((event) -> {
            if (this.categoryTreeView.getSelectionModel().getSelectedItem() != null) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setHeaderText("");
                alert.setContentText("请确认是否删除？仅删除分类信息，不会删除该分类下的网站。");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                        String cataGoryName = (this.categoryTreeView.getSelectionModel().getSelectedItem()).getValue();
                        if (this.shellManager.deleteCatagory(cataGoryName) > 0) {
                            this.statusLabel.setText("分类删除完成");
                            this.initCatagoryTree();
                        }
                    } catch (Exception var5) {
                        this.statusLabel.setText("分类删除失败：" + var5.getMessage());
                        var5.printStackTrace();
                    }
                }

            }
        });
        this.categoryTreeView.setContextMenu(treeContextMenu);
        this.categoryTreeView.setOnMouseClicked((event) -> {
            TreeItem<String> currentTreeItem = this.categoryTreeView.getSelectionModel().getSelectedItem();
            if (currentTreeItem.isLeaf()) {
                String categoryName = currentTreeItem.getValue();

                try {
                    this.shellListTable.getItems().clear();
                    JSONArray shellList = this.shellManager.findShellByCatagory(categoryName);
                    this.fillShellRows(shellList);
                } catch (Exception var6) {
                    var6.printStackTrace();
                }
            } else {
                try {
                    this.shellListTable.getItems().clear();
                    this.loadShellList();
                } catch (Exception var5) {
                    var5.printStackTrace();
                }
            }

        });
    }

    private void initCatagoryTree() throws Exception {
        JSONArray categoryList = this.shellManager.listCatagory();
        TreeItem<String> rootItem = new TreeItem<>("分类列表", new ImageView());

        for (int i = 0; i < categoryList.length(); ++i) {
            JSONObject categoryObj = categoryList.getJSONObject(i);
            TreeItem<String> treeItem = new TreeItem<>(categoryObj.getString("name"));
            rootItem.getChildren().add(treeItem);
        }

        rootItem.setExpanded(true);
        this.categoryTreeView.setRoot(rootItem);
        this.categoryTreeView.getSelectionModel().select(rootItem);
    }
}
