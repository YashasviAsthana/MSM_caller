package com.projects.mara.msm_caller;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CancellationSignal;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class MainActivity extends AppCompatActivity implements ContactsAdapter.ButtonListener {

    ArrayList<contact> selectUsers;
    ArrayList<contact> log;
    // Contact List
    ListView listView;
    // Cursor to load contacts list
    Cursor phones;
    EditText search;
    String phn,uri,operator;
    // Pop up
    ContentResolver resolver;
    ContactsAdapter adapter;
    DatabaseHelper db;
    MyReceiver myReceiver;
    FloatingActionButton addContact;
    String delId;int delPos;
    LoadContact loadContact;
    boolean flag = false,fingerOn = true;
    FingerprintManager fingerprintManager;
    KeyguardManager keyguardManager;
    contact emergencyContact;

    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///////////////////////////////////////////////////// FINGERPRINT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            if (!fingerprintManager.isHardwareDetected()) {

                Toast.makeText(MainActivity.this,"Your device doesn't support fingerprint authentication",Toast.LENGTH_SHORT).show();

            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(MainActivity.this,"No fingerprint configured. Please register at least one fingerprint in your device's Settings for Emergency call.",Toast.LENGTH_SHORT).show();
            }
        }

        /////////////////////////////////////////////////////



        addContact = (FloatingActionButton)findViewById(R.id.addContact);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                flag =true;
                startActivity(intent);
            }
        });
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED | ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_CONTACTS}, 0x12345);
        }
        getSupportActionBar().setTitle("Contacts");

        db = new DatabaseHelper(this);

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(USSDService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
        log = new ArrayList<contact>();
        selectUsers = new ArrayList<contact>();
        resolver = this.getContentResolver();
        listView = (ListView) findViewById(R.id.contacts_list);
        search = (EditText)findViewById(R.id.editText);
        loadContact = new LoadContact();
        loadContact.execute();
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);

        operator = telephonyManager.getSimOperatorName();
        //Toast.makeText(this,operator,Toast.LENGTH_LONG).show();


        final SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.USER_SHARED_PREF, "Not Available");
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Config.ECONTACT_SHARED_PREF,"Not Available");
        if(json=="Not Available")
            Toast.makeText(this,"Please set the emergency contact.",Toast.LENGTH_SHORT);
        else {
            emergencyContact = gson.fromJson(json, contact.class);
        }

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                emergencyContact = selectUsers.get(i);
                SharedPreferences.Editor prefEditor = sharedPreferences.edit();
                Gson gson = new Gson();
                String json = gson.toJson(emergencyContact);
                prefEditor.putString(Config.ECONTACT_SHARED_PREF,json);
                prefEditor.apply();
                Toast.makeText(MainActivity.this,"Emergency Contact set.",Toast.LENGTH_SHORT).show();

                return false;
            }
        });


        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapter.filter(charSequence.toString());
                    adapter.notifyDataSetChanged();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
            });

        try {
            if (getIntent().getStringExtra("call").equals("1")) {
                String number = getIntent().getStringExtra("phone");
                Log.d("Number: ",number);
                int pos;
                for (int i = 0; i < phones.getCount(); i++) {
                    if (number.equals(selectUsers.get(i).getPhone())) {
                        pos = i;
                        Log.d("Found: ",String.valueOf(pos));

                        makeCall(pos);
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }




        //Snackbar.make(new CoordinatorLayout(this),"Welcome "+username,Snackbar.LENGTH_LONG).show();
        //Toast.makeText(this,"Welcome "+username,Toast.LENGTH_LONG).show();

    }

    // Load data on background
    class LoadContact extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            //startService(new Intent(MainActivity.this,USSDService.class));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get Contact list from Phone

            if (phones != null) {
                Log.e("count", "" + phones.getCount());
                if (phones.getCount() == 0) {
                    //Toast.makeText(MainActivity.this, "No contacts in your contact list.", Toast.LENGTH_LONG).show();
                }
                while (phones.moveToNext()) {
                    String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String image_thumb = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                    Bitmap bit_thumb = null;
                    try {
                        if (image_thumb != null) {
                            bit_thumb = MediaStore.Images.Media.getBitmap(resolver, Uri.parse(image_thumb));
                        } else {
                            //Log.e("No Image Thumb", "--------------");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    contact selectUser = new contact();
                    selectUser.setName(name);
                    selectUser.setPhone(phoneNumber);
                    selectUser.setId(id);
                    selectUser.setThumb(bit_thumb);
                    selectUsers.add(selectUser);
                }
            } else {
                //Log.e("Cursor close 1", "----------------");
            }
            phones.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new ContactsAdapter(selectUsers, MainActivity.this);
            listView.setAdapter(adapter);
            adapter.setCallButtonListener(MainActivity.this);
            adapter.setDropButtonListener(MainActivity.this);
            try {
                startService(new Intent(MainActivity.this, USSDService.class));
            }catch (Exception e){
                e.printStackTrace();
            }
            //listView.setFastScrollEnabled(true);
        }
    }

    @TargetApi(23)
    @Override
    public void onStart(){
        super.onStart();
        fingerOn = true;
        if(isAccessibilityServiceEnabled(MainActivity.this,USSDService.class)){
            Log.i("Service: ","Fine");
        }
        else{
            Log.i("Service: ","Not Running");
            FragmentManager service = getFragmentManager();
            AccessibilityDialog serviceFragment = new AccessibilityDialog();
            serviceFragment.show(service, "theDialog");
        }
        try {
            if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText(MainActivity.this, "Please enable lockscreen security in your device's Settings", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }
                if (initCipher()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        FingerprintHelper helper = new FingerprintHelper(this);
                        helper.startAuth(fingerprintManager, cryptoObject);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0,720);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                addContact.setRotation(value);
            }
        });
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(5000);
        animator.setStartDelay(600);
        animator.start();

        addContact.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setPressed(false);
                        }
                    });
                }
                return false;
            }
        });
    }

    @Override
    public void onButtonClickListener(int position,String value){
        makeCall(position);
    }


    public void makeCall(int position){
        Calendar c = Calendar.getInstance();
        date = dateformat.format(c.getTime());
        time  = timeformat.format(c.getTime());
        name = selectUsers.get(position).getName();
        phn = selectUsers.get(position).getPhone();
        //writeDB();
        uri = "tel:" + phn;
        //Toast.makeText(MainActivity.this, "Button click " + value, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse(uri)));
    }

    @Override
    public void onDropClickListener(int position,String value){
        //Toast.makeText(MainActivity.this, "Drop click " + value, Toast.LENGTH_SHORT).show();
        delId = selectUsers.get(position).getId();
        delPos = position;
        FragmentManager delcontactmanager = getFragmentManager();
        DeleteContactFragment deleteContactFragment = new DeleteContactFragment();
        deleteContactFragment.show(delcontactmanager, "theDialog");
    }

    public void deleteContact(){
        ArrayList ops = new ArrayList();
        String[] args = new String[] {delId};
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI).withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args) .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }catch (Exception e){
            e.printStackTrace();
        }
        selectUsers.remove(delPos);

        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onResume(){
        super.onResume();
        fingerOn = true;
        if(flag){
            flag = false;
            startActivity(new Intent(MainActivity.this,MainActivity.class));
            finish();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(USSDService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
        //startService(new Intent(MainActivity.this,USSDService.class));
    }
    @Override
    protected void onPause(){
        super.onPause();
        fingerOn = false;
        //stopService(new Intent(MainActivity.this,USSDService.class));
    }
    @Override
    protected void onStop() {
        super.onStop();
        fingerOn = false;
        unregisterReceiver(myReceiver);
        //stopService(new Intent(MainActivity.this,USSDService.class));
        //phones.close();
    }
    SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yy");
    SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm aa");
    String date,time,name,balance;

    public void writeDB(){
        logdetails ld = new logdetails();
        ld.setDate(date);
        ld.setCallTime(time);
        ld.setName(name);
        ld.setPhone(phn);
        balance = String.valueOf(datapassed);
        //get balance and set here
        //balance = "0";
        ld.setBalance(balance);
        boolean ok = db.insertData(ld);
        //if(ok) Toast.makeText(MainActivity.this,"Inserted",Toast.LENGTH_LONG).show();
    }



    public void getBalance(){
        String ussdCode = "*123#";
        switch (operator){
            case "airtel":
                ussdCode = "*123#";
                ///////////////////////////////////////////
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(ussdToCallableUri(ussdCode));
                try{
                    startActivity(intent);
                } catch (SecurityException e){
                    e.printStackTrace();
                }
                break;
            case "idea":
                ussdCode = "*131*3#";
                break;
            case "vodafone":
                ussdCode = "*111#";
                break;
            case "reliance":
                ussdCode = "*367#";
                break;
            case "bsnl":
                ussdCode = "*123#";
                break;
            case "aircel":
                ussdCode = "*125#";
                break;
        }
    }


    private Uri ussdToCallableUri(String ussd) {
        String uriString = "";
        if(!ussd.startsWith("tel:"))
            uriString += "tel:";
        for(char c : ussd.toCharArray()) {
            if(c == '#')
                uriString += Uri.encode("#");
            else
                uriString += c;
        }
        return Uri.parse(uriString);
    }

    public class PhoneCallListener extends PhoneStateListener{

        private boolean ongoing = false;

        @Override
        public void onCallStateChanged(int state, String inccom_no){

            if(TelephonyManager.CALL_STATE_OFFHOOK == state){
                //phone active
                ongoing = true;
            }
            if(TelephonyManager.CALL_STATE_IDLE == state){
                if(ongoing){
                    //Toast.makeText(MainActivity.this,"CALL Ended",Toast.LENGTH_SHORT).show();
                    getBalance();
                    //stopService(new Intent(MainActivity.this,USSDService.class));
                    ongoing=false;
                }
            }
        }
    }
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.log:
                Intent intent = new Intent(MainActivity.this,CallLog.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                FragmentManager logoutmanager = getFragmentManager();
                LogoutDialogFragment1 logoutDialogFragment = new LogoutDialogFragment1();
                logoutDialogFragment.show(logoutmanager, "theDialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    float datapassed;
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            datapassed = arg1.getFloatExtra("bal",2f);
            //datapassed = 0;
            Toast.makeText(MainActivity.this, "Balance: " + String.valueOf(datapassed) + "\nStored in Database.", Toast.LENGTH_LONG).show();
            if(datapassed<10.00){
                lowBalAlert(datapassed);
            }
            writeDB();
        }
    }

    public void logout(){
        //logout............................................................................................
        SharedPreferences preferences = getSharedPreferences(Config.SHARED_PREF_NAME,Context.MODE_PRIVATE);
        //Getting editor
        SharedPreferences.Editor editor = preferences.edit();

        //Puting the value false for loggedin
        editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, false);

        //Putting blank value to email
        editor.putString(Config.USER_SHARED_PREF, "");

        //Saving the sharedpreferences
        editor.apply();

        finish();
        startActivity(new Intent(MainActivity.this,SignIn.class));
    }

    public void lowBalAlert(Float bal){
        NotificationCompat.Builder mbuilder = new NotificationCompat.Builder(this);

        mbuilder.setSmallIcon(R.mipmap.ic_launcher);
        mbuilder.setContentTitle("Low Balance");
        mbuilder.setContentText("Balance after your last call was Rs."+bal);

        Intent resultIntent = new Intent(this,CallLog.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this
        ,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mbuilder.setContentIntent(pendingIntent);
        int N_id = 001;
        NotificationManager nmgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nmgr.notify(N_id,mbuilder.build());
    }

    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;


    @TargetApi(23)
    private void generateKey() throws FingerprintException {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    @TargetApi(23)
    public boolean initCipher() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }


    private class FingerprintException extends Exception {

        public FingerprintException(Exception e) {
            super(e);
        }
    }


    ///// FingerprintHelper Class
    @TargetApi(23)
    public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {


        private CancellationSignal cancellationSignal;
        private Context context;
        public FingerprintManager managerG=null;
        FingerprintManager.CryptoObject cryptoObjectG=null;
        int count = 1;

        public FingerprintHelper(Context mContext) {
            context = mContext;
        }

        public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
            cancellationSignal = new CancellationSignal();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            managerG = manager;
            cryptoObjectG = cryptoObject;
            if(fingerOn) manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }

        @Override
        public void onAuthenticationError(int errMsgId,
                                          CharSequence errString) {
            //Toast.makeText(context, "Authentication error\n" + errString, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(context, "Please use a registered fingerprint.", Toast.LENGTH_LONG).show();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if(fingerOn)    managerG.authenticate(cryptoObjectG, cancellationSignal, 0, this, null);
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId,
                                         CharSequence helpString) {
            //Toast.makeText(context, "Authentication help\n" + helpString, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationSucceeded(
                FingerprintManager.AuthenticationResult result) {

            if(fingerOn) {
                //Toast.makeText(context, "Success! Count = "+count,Toast.LENGTH_LONG).show();
                count++;
                if (emergencyContact == null) {
                    Toast.makeText(MainActivity.this, "Emergency Contact not set.", Toast.LENGTH_SHORT).show();
                } else {
                    emergencyCall(emergencyContact);
                }
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                managerG.authenticate(cryptoObjectG, cancellationSignal, 0, this, null);
            }
        }
    }


    public void emergencyCall(contact ec){
        Calendar c = Calendar.getInstance();
        date = dateformat.format(c.getTime());
        time  = timeformat.format(c.getTime());
        name = ec.getName();
        phn = ec.getPhone();
        uri = "tel:" + phn;
        startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse(uri)));
    }

    @Override
    public void onBackPressed(){
        FragmentManager logoutmanager = getFragmentManager();
        LogoutDialogFragment1 logoutDialogFragment = new LogoutDialogFragment1();
        logoutDialogFragment.show(logoutmanager, "theDialog");
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }


    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }

        return false;
    }

    /*
    float x1,y1;
    float x2,y2;
    public boolean onTouchEvent(MotionEvent motionEvent){
        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                x1 = motionEvent.getX();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                x2 = motionEvent.getX();
                y2 = motionEvent.getY();
                if (x1 < x2)
                {
                    //Toast.makeText(this, "Left to Right Swap Performed", Toast.LENGTH_LONG).show();
                }
                if (x1 > x2)
                {
                    //Toast.makeText(this, "Right to Left Swap Performed", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(MainActivity.this,CallLog.class);
                    startActivity(i);
                }
                break;
            }
        }

        return false;
    }
    */
}

