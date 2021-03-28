package com.openclassrooms.go4lunch.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.openclassrooms.go4lunch.worker.ReconnectionWorker;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.mam.MamManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static androidx.work.WorkRequest.MIN_BACKOFF_MILLIS;
import static com.openclassrooms.go4lunch.utils.Constante.CREATE_USER_EJABBERD;
import static com.openclassrooms.go4lunch.utils.Constante.HOST;
import static com.openclassrooms.go4lunch.utils.Constante.LOG_IN_EJABBERD;
import static com.openclassrooms.go4lunch.utils.Constante.LOG_OUT_EJABBERD;
import static com.openclassrooms.go4lunch.utils.Constante.PACKAGE_NAME;
import static com.openclassrooms.go4lunch.utils.Constante.PORT;
import static com.openclassrooms.go4lunch.utils.Constante.RECEIVED_MESSAGE;
import static com.openclassrooms.go4lunch.utils.Constante.SENT_MESSAGE_XMPP;

public class XmppConnection implements ConnectionListener {

    private final Context appliContext;
    private final WorkManager workManager;
    private XMPPTCPConnection mConnection;
    private BroadcastReceiver uiThreadMessageReceiver;
    private MamManager mamManager;

    public XmppConnection(Context context) {
        appliContext = context.getApplicationContext();
        workManager = WorkManager.getInstance(context);
    }

    public void connect() throws InterruptedException, IOException, SmackException, XMPPException {
        connection();

        setupUiThreadBrodcastMessageReceiver();
        receivedMessage(mConnection);
    }

    public void connection() throws InterruptedException, XMPPException, SmackException, IOException {
        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
        try {
            builder.setPort(PORT)
                    .setXmppDomain(HOST)
                    //.setHostAddress(InetAddress.getByName("c11.it-local"))
                    .setHostAddress(InetAddress.getByName(HOST))
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            Log.e("errorXmpp", "error : " + e.getMessage());
        }

        mConnection = new XMPPTCPConnection(builder.build());
        mConnection.addConnectionListener(this);
        mConnection.connect();
        mamManager = MamManager.getInstanceFor(mConnection);
        mConnection.setUseStreamManagement(true);
        mConnection.setUseStreamManagementResumption(true);
        /*mConnection.setPreferredResumptionTime(5);
        ReconnectionManager.getInstanceFor(mConnection).enableAutomaticReconnection();
        PingManager.getInstanceFor(mConnection).setPingInterval(10);
        mConnection.isDisconnectedButSmResumptionPossible();*/

    }

    public void reconnection() throws InterruptedException, IOException, SmackException, XMPPException {
        connection();
        receivedMessage(mConnection);

    }

    public void receivedMessage(XMPPTCPConnection mConnection) {
        ChatManager.getInstanceFor(mConnection).addIncomingListener((from, message, chat) -> {
            String messageFrom = message.getFrom().toString();

            String contactJid = "";
            if (messageFrom.contains("/")) {
                contactJid = messageFrom.split("/")[0];
            } else {
                contactJid = messageFrom;
            }

            Intent intent = new Intent(RECEIVED_MESSAGE);
            intent.setPackage(PACKAGE_NAME);
            intent.putExtra("receiveJid", contactJid);

            intent.putExtra("receivedMessage", message.getBody());
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            appliContext.sendBroadcast(intent);
        });
    }

    public void disconnect() {
        if (mConnection != null) {
            mConnection.disconnect();
        }
        mConnection = null;
    }

    public void createEjabberdAccount(String userName, String password) {
        AccountManager accountManager = AccountManager.getInstance(mConnection);
        try {
            if (accountManager.supportsAccountCreation()) {
                accountManager.sensitiveOperationOverInsecureConnection(true);
                accountManager.createAccount(Localpart.from(userName), password);
            }
        } catch (InterruptedException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | SmackException.NoResponseException | XmppStringprepException e) {
            Log.e("createAccount", "error : " + e.getMessage());
        }
    }

    public void logIn(String username, String psswd) throws InterruptedException, IOException, SmackException, XMPPException {
        mConnection.login(username, psswd);
    }

    public void logOut() {
        mConnection.disconnect();
    }

    private void setupUiThreadBrodcastMessageReceiver() {
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(SENT_MESSAGE_XMPP)) {
                    sendMessage(intent.getStringExtra("messageToXmpp"), intent.getStringExtra("jidToXmpp"));
                } else if (action.equals(CREATE_USER_EJABBERD)) {
                    Log.d("TestCreate", "Connexion" + intent.getStringExtra("ejabberdName"));
                    createEjabberdAccount(intent.getStringExtra("ejabberdName"), intent.getStringExtra("ejabberdPasswd"));
                } else if (action.equals(LOG_IN_EJABBERD)) {
                    try {
                        logIn(intent.getStringExtra("userJid"), intent.getStringExtra("psswdJid"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SmackException e) {
                        e.printStackTrace();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }

                } else if (action.equals(LOG_OUT_EJABBERD)) {
                    logOut();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(SENT_MESSAGE_XMPP);
        filter.addAction(CREATE_USER_EJABBERD);
        filter.addAction(LOG_IN_EJABBERD);
        filter.addAction(LOG_OUT_EJABBERD);
        appliContext.registerReceiver(uiThreadMessageReceiver, filter);
    }

    private void sendMessage(String messageBody, String messageReceiver) {
        EntityBareJid jid = null;

        ChatManager chatmanager = ChatManager.getInstanceFor(mConnection);

        try {
            jid = JidCreate.entityBareFrom(messageReceiver);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        Chat chat = chatmanager.chatWith(jid);

        try {
            Message message = new Message(jid, Message.Type.chat);
            message.setBody(messageBody);
            chat.send(message);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connected(XMPPConnection connection) {
        ConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d("BootB", "OkConnecte");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        ConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d("BootB", "OkAuth");
        //showChatActivityWhenAuthenticate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void connectionClosed() {
        ConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d("discoError", "connection closed");

        OneTimeWorkRequest reconnection = new OneTimeWorkRequest.Builder(ReconnectionWorker.class)
                .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS)
                .build();

        workManager.enqueue(reconnection);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void connectionClosedOnError(Exception e) {
        ConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d("discoError", "disconnection on error");

        OneTimeWorkRequest reconnection = new OneTimeWorkRequest.Builder(ReconnectionWorker.class)
                .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS)
                .build();

        workManager.enqueue(reconnection);
    }

    public enum ConnectionState {
        CONNECTED, DISCONNECTED
    }

    public enum LoggedInState {
        LOGGED_OUT
    }

}
