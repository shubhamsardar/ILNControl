package directory.tripin.com.tripindirectory.chatingactivities;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.keiferstone.nonet.NoNet;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import directory.tripin.com.tripindirectory.chatingactivities.models.ChatHeadPojo;
import directory.tripin.com.tripindirectory.chatingactivities.models.ChatIndicatorPojo;
import directory.tripin.com.tripindirectory.chatingactivities.models.ChatItemPojo;
import directory.tripin.com.tripindirectory.chatingactivities.models.ChatItemViewHolder;
import directory.tripin.com.tripindirectory.chatingactivities.models.UserActivityPojo;
import directory.tripin.com.tripindirectory.chatingactivities.models.UserPresensePojo;
import directory.tripin.com.tripindirectory.newlookcode.FacebookRequiredActivity;
import directory.tripin.com.tripindirectory.newlookcode.pojos.InteractionPojo;
import directory.tripin.com.tripindirectory.newlookcode.pojos.UserProfile;
import directory.tripin.com.tripindirectory.activity.PartnerDetailScrollingActivity;
import directory.tripin.com.tripindirectory.R;
import directory.tripin.com.tripindirectory.helper.CircleTransform;
import directory.tripin.com.tripindirectory.helper.ListPaddingDecoration;
import directory.tripin.com.tripindirectory.helper.Logger;
import directory.tripin.com.tripindirectory.manager.PreferenceManager;
import directory.tripin.com.tripindirectory.newlookcode.utils.MixPanelConstants;
import directory.tripin.com.tripindirectory.newprofiles.activities.CompanyProfileDisplayActivity;
import directory.tripin.com.tripindirectory.newprofiles.models.RateReminderPojo;
import directory.tripin.com.tripindirectory.utils.TextUtils;

public class ChatRoomActivity extends AppCompatActivity {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    FirestoreRecyclerAdapter<ChatItemPojo, ChatItemViewHolder> adapter;
    private RecyclerView mChatsList;
    private EditText mChatEditText;
    private ImageButton mSendAction;
    private ConstraintLayout mChatIntiatorLayout;
    private TextView mInitiatorMsg;
    LottieAnimationView loading;
    LinearLayoutManager mLayoutManagerChats;
    DatabaseReference databaseReference;
    ValueEventListener userpresencelistner;
    ValueEventListener useractivity;
    ValueEventListener pendingMsgs;

    ConstraintLayout mTypingView;
    ImageView mTypingThumnail;
    ImageView mMainThumbnail;
    ImageView mMainBack;
    ImageView mCall;
    TextView mTitle;
    TextView mSubtitle;
    ImageButton mCancelImsg;
    SimpleDateFormat simpleDateFormat;
    private FirebaseAnalytics firebaseAnalytics;
    private MixpanelAPI mixpanelAPI;
    NotificationManager notificationManager;


    private String mChatRoomId;
    private String iMsg = "";
    private ChatIndicatorPojo mOpponentIndicator;
    private ChatIndicatorPojo mMyIndicator;
    private int howmanyread = 0;



    //opponents necessary information
    private String mORMN;
    private String mOUID;
    private String mOFUID = "";


    //collected opponent user info
    private String mOpponentCompName = "";
    private String mOpponentImageUrl = "";
    private String mOpponentFcm;

    //my info
    private String mMyCompName = "";
    private String mMyImageUrl = "";
    private String mMyFcm;
    private FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;


    TextUtils textUtils;
    ListPaddingDecoration listPaddingDecoration;


    private int mMsgType = 0;
    boolean isEtTapped = false;
    boolean isTyping = false;
    String mSubTitleText = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        simpleDateFormat = new SimpleDateFormat("hh:mm, EEE", Locale.ENGLISH);
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = PreferenceManager.getInstance(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mixpanelAPI = MixpanelAPI.getInstance(this,MixPanelConstants.MIXPANEL_TOKEN);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Checking Auth, Finish if not logged in

        if (mAuth.getCurrentUser() == null
                || FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() == null
                || !preferenceManager.isFacebooked()) {
            Intent i = new Intent(ChatRoomActivity.this, FacebookRequiredActivity.class);
            i.putExtra("from","Chat");
            startActivityForResult(i,3);
            Toast.makeText(getApplicationContext(), "Login with Facebook To chat", Toast.LENGTH_LONG).show();
        }

        mOpponentIndicator = new ChatIndicatorPojo(mMyImageUrl,0);
        mMyIndicator = new ChatIndicatorPojo(mMyImageUrl,0);

        setChatListUI();
        setUI();
        setListners();
        internetCheck();


    }


    private void setChatListUI() {
        mChatsList = findViewById(R.id.rv_chat);
        mLayoutManagerChats = new LinearLayoutManager(this);
        mLayoutManagerChats.setReverseLayout(true);
        //mLayoutManagerChats.setStackFromEnd(true);
        mChatsList.setLayoutManager(mLayoutManagerChats);
        listPaddingDecoration = new ListPaddingDecoration(getApplicationContext());
        //mChatsList.addItemDecoration(listPaddingDecoration);
    }

    private void setUI() {
        mSendAction = findViewById(R.id.imageButtonSendAction);
        mChatEditText = findViewById(R.id.et_msg);
        mInitiatorMsg = findViewById(R.id.textViewimsg);
        mChatIntiatorLayout = findViewById(R.id.cl_imsg);
        loading = findViewById(R.id.loading);
        mTypingThumnail = findViewById(R.id.imageViewThumbTyping);
        mTypingView = findViewById(R.id.cl_typing);
        mCancelImsg = findViewById(R.id.imageButtonCancelImsg);
        mMainThumbnail = findViewById(R.id.mainhumb);
        mMainBack = findViewById(R.id.back);
        mSubtitle = findViewById(R.id.status);
        mTitle = findViewById(R.id.opponentname);
        mCall = findViewById(R.id.call);
        textUtils = new TextUtils();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListners() {

        mCancelImsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iMsg = "";
                mChatIntiatorLayout.setVisibility(View.GONE);
                Bundle bundle = new Bundle();
                firebaseAnalytics.logEvent("z_imsg_canceled", bundle);
            }
        });
        mChatEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    updateTypingStatus(false);
                }
            }
        });

        mChatEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isEtTapped = true;

                return false;
            }
        });
        mChatEditText.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (isEtTapped) {
                    mLayoutManagerChats.scrollToPosition(0);
                    isEtTapped = false;
                    Logger.v("onLayout change has focus");

                }
            }
        });
        mChatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Logger.v("before");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    mSendAction.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_send_white_24dp));

                } else {
                    mSendAction.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mic_white_24dp));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty() && editable.toString().trim().length() == 1) {

                    //Log.i(TAG, “typing started event…”);

                    isTyping = true;
                    updateTypingStatus(isTyping);

                    //send typing started status

                } else if (editable.toString().trim().length() == 0 && isTyping) {

                    //Log.i(TAG, “typing stopped event…”);

                    isTyping = false;
                    updateTypingStatus(isTyping);

                    //send typing stopped status

                }

            }
        });

        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoDetailsActivity();
            }
        });
        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNumber(mORMN);
            }
        });
        mMainBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSendAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChatEditText.getText().toString().isEmpty()) {
                    //voice
                    startVoiceRecognitionActivity();
                } else {
                    //compose and send message


                    if (!iMsg.isEmpty()) {
                        //send msg with imsg
                        mChatIntiatorLayout.setVisibility(View.GONE);

                        ChatItemPojo chatItemPojo = new ChatItemPojo(preferenceManager.getUserId(), preferenceManager.getFuid(), preferenceManager.getImageUrl(),
                                mOpponentImageUrl,
                                mOUID,
                                mMyFcm,
                                mOpponentFcm,
                                preferenceManager.getRMN(),
                                mORMN,
                                preferenceManager.getFuid(),
                                mMyCompName,
                                mOpponentCompName,
                                iMsg, mChatRoomId,
                                0, 2);

                        FirebaseFirestore.getInstance().collection("chats").document("chatrooms").collection(mChatRoomId).add(chatItemPojo).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                Bundle bundle = new Bundle();
                                if (mSubTitleText.equals("Active Now")) {
                                    bundle.putString("opponent_active", "Yes");
                                } else {
                                    bundle.putString("opponent_active", "No");
                                }

                                bundle.putString("msg_type", "InfoMsg");
                                firebaseAnalytics.logEvent("z_chat_send_action", bundle);
                                trackSendeventonMixPanel();
                                iMsg = "";
                                mSendAction.setClickable(false);


                                ChatItemPojo chatItemPojo = new ChatItemPojo(preferenceManager.getUserId(), preferenceManager.getFuid(), preferenceManager.getImageUrl(),
                                        mOpponentImageUrl,
                                        mOUID,
                                        mMyFcm,
                                        mOpponentFcm,
                                        preferenceManager.getRMN(),
                                        mORMN,
                                        preferenceManager.getFuid(),
                                        mMyCompName,
                                        mOpponentCompName,
                                        mChatEditText.getText().toString().trim(),
                                        mChatRoomId,
                                        0, mMsgType);

                                final String lastmsg = mChatEditText.getText().toString().trim();
                                mChatEditText.setText("");

                                FirebaseFirestore.getInstance().collection("chats").document("chatrooms").collection(mChatRoomId).add(chatItemPojo).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {

                                        Bundle bundle = new Bundle();
                                        if (mSubTitleText.equals("Active Now")) {
                                            bundle.putString("opponent_active", "Yes");
                                        } else {
                                            bundle.putString("opponent_active", "No");
                                        }
                                        bundle.putString("msg_type", "Normal");
                                        firebaseAnalytics.logEvent("z_chat_send_action", bundle);
                                        trackSendeventonMixPanel();

                                        mSendAction.setClickable(true);
                                        ChatHeadPojo chatHeadPojo = new ChatHeadPojo(mChatRoomId,
                                                preferenceManager.getRMN(),
                                                preferenceManager.getUserId(),
                                                preferenceManager.getFuid(),
                                                lastmsg,
                                                mMyImageUrl
                                                , mMyCompName);
                                        FirebaseFirestore.getInstance().collection("chats").document("chatheads").collection(mOUID).document(mAuth.getUid()).set(chatHeadPojo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                ChatHeadPojo chatHeadPojo = new ChatHeadPojo(mChatRoomId,
                                                        mORMN,
                                                        mOUID,
                                                        mOFUID,
                                                        lastmsg,
                                                        mOpponentImageUrl
                                                        , mOpponentCompName);

                                                FirebaseFirestore.getInstance().collection("chats").document("chatheads").collection(mAuth.getUid()).document(mOUID).set(chatHeadPojo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Logger.v("heads updated");
                                                        mOpponentIndicator.setmMsgCount(mOpponentIndicator.getmMsgCount()+2);
                                                        databaseReference.child("chatpresence").child("chatpendings").child(mOUID).setValue(mOpponentIndicator).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Logger.v("opponent indicator updated");
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });

                                    }
                                });
                            }
                        });


                    } else {
                        //only msg
                        mSendAction.setClickable(false);
                        ChatItemPojo chatItemPojo = new ChatItemPojo(preferenceManager.getUserId(), preferenceManager.getFuid(), preferenceManager.getImageUrl(),
                                mOpponentImageUrl,
                                mOUID,
                                mMyFcm,
                                mOpponentFcm,
                                preferenceManager.getRMN(),
                                mORMN,
                                preferenceManager.getFuid(),
                                mMyCompName,
                                mOpponentCompName,
                                mChatEditText.getText().toString().trim(),
                                mChatRoomId,
                                0, mMsgType);

                        final String lastmsg = mChatEditText.getText().toString().trim();
                        mChatEditText.setText("");

                        FirebaseFirestore.getInstance().collection("chats").document("chatrooms").collection(mChatRoomId).add(chatItemPojo).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                Bundle bundle = new Bundle();
                                if (mSubTitleText.equals("Active Now")) {
                                    bundle.putString("opponent_active", "Yes");
                                } else {
                                    bundle.putString("opponent_active", "No");
                                }
                                bundle.putString("msg_type", "Normal");
                                firebaseAnalytics.logEvent("z_chat_send_action", bundle);
                                trackSendeventonMixPanel();

                                mSendAction.setClickable(true);
                                ChatHeadPojo chatHeadPojo = new ChatHeadPojo(mChatRoomId,
                                        preferenceManager.getRMN(),
                                        preferenceManager.getUserId(),
                                        preferenceManager.getFuid(),
                                        lastmsg,
                                        mMyImageUrl
                                        , mMyCompName);
                                FirebaseFirestore.getInstance().collection("chats").document("chatheads").collection(mOUID).document(mAuth.getUid()).set(chatHeadPojo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ChatHeadPojo chatHeadPojo = new ChatHeadPojo(mChatRoomId,
                                                mORMN,
                                                mOUID,
                                                mOFUID,
                                                lastmsg,
                                                mOpponentImageUrl
                                                , mOpponentCompName);

                                        FirebaseFirestore.getInstance().collection("chats").document("chatheads").collection(mAuth.getUid()).document(mOUID).set(chatHeadPojo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Logger.v("heads updated");
                                                mOpponentIndicator.setmMsgCount(mOpponentIndicator.getmMsgCount()+1);
                                                mOpponentIndicator.setmLastMsgUserImageUrl(preferenceManager.getImageUrl());
                                                databaseReference.child("chatpresence").child("chatpendings").child(mOUID).setValue(mOpponentIndicator).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Logger.v("opponent indicator updated");
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });

                            }
                        });
                    }

                }
            }
        });
        getIntentData();
    }

    private void trackSendeventonMixPanel() {

        JSONObject props = new JSONObject();
        try {
            props.put("ormn", mORMN);
            props.put("ostatus", mSubTitleText);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanelAPI.track(MixPanelConstants.EVENT_CHAT_SEND, props);
    }


    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("ormn") != null &&
                    bundle.getString("ouid") != null) {
                mORMN = bundle.getString("ormn");
                mTitle.setText(mORMN);
                mOUID = bundle.getString("ouid");
                //check if there is any i msg
                if (bundle.getString("imsg") != null) {
                    iMsg = bundle.getString("imsg");
                    if (!iMsg.isEmpty()) {
                        mInitiatorMsg.setText(iMsg);
                        mChatIntiatorLayout.setVisibility(View.VISIBLE);
                    }
                }
                if (bundle.getString("ofuid") != null) {
                    if (!bundle.getString("ofuid").isEmpty()) {
                        mOFUID = bundle.getString("ofuid");
                    } else {
                        mSubtitle.setText(R.string.userInactive);
                    }
                } else {
                    mSubtitle.setText(R.string.userInactive);
                }
                getMyDetails();

            } else {
                Logger.v("ormn or ouid null");
                finish();
            }

//            if(bundle.getInt("nid",0)!=0){
//                notificationManager.cancel(bundle.getInt("nid"));
//            }

        } else {
            Logger.v("bundle null");
            finish();
        }
    }

    private void getMyDetails() {
        mMyCompName = preferenceManager.getDisplayName();
        mMyFcm = preferenceManager.getFcmToken();
        mMyImageUrl = preferenceManager.getImageUrl();
        getOpponentsDetails(mOFUID);

    }


    private void getOpponentsDetails(String ofuid) {
        Logger.v("get Opponent Details: " + ofuid);
        Logger.v("muid: " + mAuth.getUid());
        Logger.v("ouid: " + mOUID);

        if (!ofuid.isEmpty()) {
            FirebaseDatabase.getInstance().getReference().child("user_profiles").child(ofuid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        mOpponentImageUrl = userProfile.getmImageUrl();
                        Logger.v("Image URL : " + mOpponentImageUrl);
                        mOpponentFcm = userProfile.getmFCM();
                        Logger.v("mOpponentFcm : " + mOpponentFcm);

                        mOpponentCompName = userProfile.getmDisplayName();
                        Logger.v("DisplayName : " + mOpponentCompName);
                        if (!mOpponentCompName.isEmpty())
                            mTitle.setText(mOpponentCompName);

                        if(mAuth.getUid()==null){
                            Toast.makeText(getApplicationContext(),"Some Error Occured, Try Login Again!",Toast.LENGTH_LONG).show();
                            finish();
                        }


                        FirebaseFirestore.getInstance().collection("chats").document("chatheads").collection(mAuth.getUid()).document(mOUID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Logger.v("onSuccess: Checking Chat Heads");
                                if (documentSnapshot.exists()) {
                                    ChatHeadPojo chatHeadPojo = documentSnapshot.toObject(ChatHeadPojo.class);
                                    assert chatHeadPojo != null;
                                    mChatRoomId = chatHeadPojo.getmChatRoomId();
                                } else {
                                    mChatRoomId = mAuth.getUid() + mOUID;
                                }
                                Logger.v("mChatRoomId: " + mChatRoomId);

                                //chat profile analytics
                                updateUserProfileAnalytics();


                                buildAdapter(mChatRoomId);

                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Logger.v("onFailure: Checking Chat Heads");
                                mChatRoomId = mAuth.getUid() + mOUID;
                                Logger.v("mChatRoomId: " + mChatRoomId);

                                //chat profile analytics
                                updateUserProfileAnalytics();

                                buildAdapter(mChatRoomId);

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "User Unavailable to Chat", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            //User Never Loged In using FB :Create Chatroom and call sms api
//            Toast.makeText(getApplicationContext(),"SMS will be sent",Toast.LENGTH_SHORT).show();

            mChatRoomId = mAuth.getUid() + mOUID;
            //chat profile analytics
            updateUserProfileAnalytics();
            buildAdapter(mChatRoomId);


        }


//        FirebaseFirestore.getInstance().collection("partners").document(ouid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                if (documentSnapshot.exists()) {
//                    PartnerInfoPojo mOpppontInfo = documentSnapshot.toObject(PartnerInfoPojo.class);
//
//                    if (!mOpppontInfo.getmCompanyName().isEmpty()) {
//                        mOpponentCompName = textUtils.toTitleCase(mOpppontInfo.getmCompanyName());
//                        setTitle(textUtils.toTitleCase(mOpppontInfo.getmCompanyName()));
//                    }
//
//                    if (mOpppontInfo.getmImagesUrl() != null) {
//                        mOpponentImageUrl = mOpppontInfo.getmImagesUrl().get(2);
//                        //setTypingThumbnail(mOpponentImageUrl);
//                        //setActionbarImage(mOpponentImageUrl);
//                    }
//
//                    mOpponentFcm = mOpppontInfo.getmFcmToken();
//
//                    FirebaseFirestore.getInstance().collection("partners").document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//
//
//                        @Override
//                        public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                            if (documentSnapshot.exists()) {
//                                PartnerInfoPojo mInfo = documentSnapshot.toObject(PartnerInfoPojo.class);
//
//                                if (!mInfo.getmCompanyName().isEmpty()) {
//                                    mMyCompName = mInfo.getmCompanyName();
//                                }
//
//                                if (mInfo.getmImagesUrl() != null) {
//                                    mMyImageUrl = mInfo.getmImagesUrl().get(2);
//                                }
//                                mMyFcm = mInfo.getmFcmToken();
//
//
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Add Your Company Info First", Toast.LENGTH_LONG).show();
//                                startActivity(new Intent(ChatRoomActivity.this, CompanyInfoActivity.class));
//                            }
//
//                        }
//                    });
//
//
//                } else {
//                    Logger.v("opponents doc dont exist");
//                    finish();
//                }
//            }
//        });


    }

    private void updateUserProfileAnalytics() {
        //chat profile analytics
        InteractionPojo interactionPojo = new  InteractionPojo(preferenceManager.getUserId(),
                preferenceManager.getFuid(),
                preferenceManager.getRMN(),
                preferenceManager.getComapanyName(), preferenceManager.getDisplayName(),
                preferenceManager.getFcmToken(), mOUID,mOFUID,mORMN,mOpponentCompName,mOpponentCompName,mOpponentFcm);



        FirebaseFirestore.getInstance().collection("partners")
                .document(mOUID)
                .collection("mChatsDump").document(getDateString())
                .collection("interactors").document(preferenceManager.getUserId()).set(interactionPojo);
    }

    private String getDateString() {
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    }


    private void setOpponentPresenceListners() {

        userpresencelistner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserPresensePojo userPresensePojo = dataSnapshot.getValue(UserPresensePojo.class);
                    if (userPresensePojo != null) {
                        if (userPresensePojo.getActive()) {
                            mSubTitleText = "Active Now";
                            mSubtitle.setText(mSubTitleText);
                            if(mChatRoomId!=null){
                                if(userPresensePojo.getmChatroomId().equals(mChatRoomId)){
                                    //with you
                                    mSubtitle.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.cyan_700));
                                }else {
                                    //not with you
                                    mSubtitle.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.blue_grey_600));
                                }
                            }

                        } else {
                            Date date = new Date(userPresensePojo.getmTimeStamp());
                            mSubTitleText = "Active " + gettimeDiff(date);
                            mSubtitle.setText(mSubTitleText);
                        }
                    }

                }else {
                    if(!mOFUID.isEmpty()){
                        mSubtitle.setText(R.string.available);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseReference.child("chatpresence").child("users").child(mOUID).addValueEventListener(userpresencelistner);
        setOpponentTypingListner();

    }

    private void setOpponentTypingListner() {
        useractivity = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    UserActivityPojo userActivityPojo = dataSnapshot.getValue(UserActivityPojo.class);
                    if (userActivityPojo != null) {
                        if (userActivityPojo.getTyping()) {
                            //set typing visible
                            mTypingView.setVisibility(View.VISIBLE);
                            mSubtitle.setText("Typing...");

                        } else {
                            //set typing invisible
                            mTypingView.setVisibility(View.GONE);
                            mSubtitle.setText(mSubTitleText);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.child("chatpresence").child("chatrooms").child(mChatRoomId).child(mOUID).addValueEventListener(useractivity);
        setOpponentPendingsListners();
    }

    private void setOpponentPendingsListners() {
        pendingMsgs = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    ChatIndicatorPojo chatIndicatorPojo = dataSnapshot.getValue(ChatIndicatorPojo.class);
                    if (chatIndicatorPojo != null) {
                        mOpponentIndicator.setmMsgCount(chatIndicatorPojo.getmMsgCount());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.child("chatpresence").child("chatpendings").child(mOUID).addValueEventListener(pendingMsgs);

    }

    private void updateUserPresence(boolean b) {
        if(mAuth.getCurrentUser()!=null){
            if(b){
                UserPresensePojo userPresensePojo = new UserPresensePojo(b, new Date().getTime(), mChatRoomId);
                databaseReference.child("chatpresence").child("users").child(mAuth.getUid()).setValue(userPresensePojo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Logger.v("onResume userpresence updated");
                    }
                });
            }else {
                UserPresensePojo userPresensePojo = new UserPresensePojo(true, new Date().getTime(), "");
                databaseReference.child("chatpresence").child("users").child(mAuth.getUid()).setValue(userPresensePojo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Logger.v("onResume userpresence updated1");
                    }
                });
                UserPresensePojo userPresensePojo2 = new UserPresensePojo(b, new Date().getTime(), mChatRoomId);
                databaseReference.child("chatpresence").child("users").child(mAuth.getUid()).onDisconnect().setValue(userPresensePojo2).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Logger.v("onResume userpresence updated");
                    }
                });
            }

        }else {
            Toast.makeText(getApplicationContext(),"Not Signed In!",Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void gotoDetailsActivity() {

        Intent intent = new Intent(ChatRoomActivity.this, CompanyProfileDisplayActivity.class);
        intent.putExtra("uid", mOUID);
        intent.putExtra("fuid", mOFUID);
        intent.putExtra("rmn", mORMN);
        intent.putExtra("fromchat",true);
        startActivity(intent);

        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent("z_chat_goto_details", bundle);
    }


    private void updateTypingStatus(final boolean isTyping) {
        UserActivityPojo userActivityPojo = new UserActivityPojo(isTyping);
        databaseReference.child("chatpresence").child("chatrooms").child(mChatRoomId).child(mAuth.getUid()).setValue(userActivityPojo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Logger.v("typing status updated : " + isTyping);
            }
        });

    }

    private void buildAdapter(final String mChatRoomId) {

        updateUserPresence(true);
        setOpponentPresenceListners();

        Query query = FirebaseFirestore.getInstance()
                .collection("chats")
                .document("chatrooms")
                .collection(mChatRoomId)
                .orderBy("mTimeStamp", Query.Direction.DESCENDING).limit(100);

        FirestoreRecyclerOptions<ChatItemPojo> options = new FirestoreRecyclerOptions.Builder<ChatItemPojo>()
                .setQuery(query, ChatItemPojo.class)
                .setLifecycleOwner(this)
                .build();

        adapter = new FirestoreRecyclerAdapter<ChatItemPojo, ChatItemViewHolder>(options) {

            @Override
            public int getItemViewType(int position) {
                if (getItem(position).getmSendersUid().equals(mAuth.getUid())) {
                    return 1;
                } else {
                    return 2;
                }
            }

            @Override
            public ChatItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view;

                switch (viewType) {
                    case 1: {
                        view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_chat_yours, parent, false);
                        return new ChatItemViewHolder(view);
                    }
                    case 2: {
                        view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_chat_opponents, parent, false);
                        return new ChatItemViewHolder(view);
                    }
                    default: {
                        return null;
                    }
                }

            }

            @Override
            protected void onBindViewHolder(final ChatItemViewHolder holder, int position, ChatItemPojo model) {

                Logger.v("onBind " + position + " " + model.getmChatMesssage());

                DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
                final String docId = snapshot.getId();

                holder.msg.setText(model.getmChatMesssage());
                Linkify.addLinks(holder.msg, Linkify.ALL);

                if (model.getmTimeStamp() != null){
                    holder.time.setText(simpleDateFormat.format(model.getmTimeStamp()));

                }


                if (model.getmMessageType() == 2) {
                    holder.msg.setTextSize(10);
                } else {
                    holder.msg.setTextSize(15);
                }

                if (model.getmSendersUid().equals(mAuth.getUid())) {
                    //my message
                    if (model.getmMessageStatus() == 1) {
                        Logger.v(model.getmChatMesssage() + " : is seen..");
                        holder.seenEye
                                .setImageDrawable(ContextCompat
                                        .getDrawable(getApplicationContext(),
                                                R.drawable.ic_visibility_red_24dp));
                    } else {
                        Logger.v(model.getmChatMesssage() + " : is not seen");
                        holder.seenEye
                                .setImageDrawable(ContextCompat
                                        .getDrawable(getApplicationContext(),
                                                R.drawable.ic_visibility_grey_24dp));
                    }
                } else {
                    //opponents message
                    if (model.getmMessageStatus() != 1) {
                        FirebaseFirestore.getInstance()
                                .collection("chats")
                                .document("chatrooms")
                                .collection(mChatRoomId)
                                .document(docId)
                                .update("mMessageStatus", 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                howmanyread = howmanyread+1;
                            }
                        });
                    }

                    if (!mOpponentImageUrl.isEmpty()) {
                        Picasso.with(holder.thumbnail.getContext())
                                .load(mOpponentImageUrl)
                                .placeholder(ContextCompat.getDrawable(getApplicationContext()
                                        , R.mipmap.ic_launcher_round))
                                .transform(new CircleTransform())
                                .fit()
                                .into(holder.thumbnail, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Logger.v("image set: " + position);
                                    }

                                    @Override
                                    public void onError() {

                                    }

                                });
                    }


//                    if (model.getmMessageType() == 1) {
//                        holder.thumbnail.setVisibility(View.INVISIBLE);
//                    } else {
//                        if (!mOpponentImageUrl.isEmpty()) {
//                            Picasso.with(holder.thumbnail.getContext())
//                                    .load(mOpponentImageUrl)
//                                    .placeholder(ContextCompat.getDrawable(getApplicationContext()
//                                            , R.drawable.ic_insert_comment_black_24dp))
//                                    .transform(new CircleTransform())
//                                    .fit()
//                                    .into(holder.thumbnail, new Callback() {
//                                        @Override
//                                        public void onSuccess() {
//                                            Logger.v("image set: " + position);
//                                        }
//
//                                        @Override
//                                        public void onError() {
//
//                                        }
//
//                                    });
//
//                        }
//                    }


                }
            }

            @Override
            public void onDataChanged() {
                mLayoutManagerChats.scrollToPosition(0);
                loading.setVisibility(View.GONE);
                mChatEditText.setVisibility(View.VISIBLE);
                if (adapter.getItemCount() > 0) {
                    if (getItem(0) != null) {
                        Logger.v("onDataChanged " + getItem(0).getmChatMesssage());
                        if (getItem(0).getmSendersUid() != null) {
                            if (getItem(0).getmSendersUid().equals(mAuth.getUid())) {
                                //my msg at last
                                mTypingThumnail.setVisibility(View.VISIBLE);
                                mMsgType = 1;
                            } else {
                                mTypingThumnail.setVisibility(View.VISIBLE);
                                //opponents msg at last
                                mMsgType = 0;
                            }
                        } else {
                            Logger.v("onData Changed: senders uid null");
                        }

                    }

                }

                if (!mOpponentImageUrl.isEmpty())
                    setThumbnail(mOpponentImageUrl);

                super.onDataChanged();


            }
        };

        mChatsList.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mChatRoomId != null) {
            updateUserPresence(true);
            setOpponentPresenceListners();
        }
        notificationManager.cancel(6);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mChatRoomId != null) {
            if (!mChatRoomId.isEmpty()) {

                updateUserPendings();
                updateUserPresence(false);
                updateTypingStatus(false);
                removeuserpresencelistners();
            }
        }
    }



    @Override
    protected void onStop() {
        super.onStop();

    }

    public void startVoiceRecognitionActivity() {
        String voiceSearchDialogTitle = "Message By Voice! ";

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                voiceSearchDialogTitle);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent("z_chat_voice_clicked", bundle);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it
            // could have heard
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String enquiry = matches.get(0).toString();
            mChatEditText.setText(enquiry);
            Bundle bundle = new Bundle();
            firebaseAnalytics.logEvent("z_chat_voice_used", bundle);
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (requestCode == 3) {
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public String gettimeDiff(Date startDate) {

        String diff = "";

        if (startDate != null) {

            Date endDate = new Date();

            long duration = endDate.getTime() - startDate.getTime();
            long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);

            if (diffInSeconds <= 0) {
                return "Today";
            }

            if (diffInSeconds < 60) {
                diff = "" + diffInSeconds + "sec ago";
            } else if (diffInMinutes < 60) {
                diff = "" + diffInMinutes + "min ago";
            } else if (diffInHours < 24) {
                diff = "" + diffInHours + "hrs ago";
            } else {

                long daysago = duration / (1000 * 60 * 60 * 24);
                diff = "" + daysago + "days ago";
            }

        }
        return diff;

    }

    public void removeuserpresencelistners() {

        if (userpresencelistner != null) {
            databaseReference.child("chatpresence").child("users").child(mOUID).removeEventListener(userpresencelistner);
        }

        if (useractivity != null) {
            databaseReference.child("chatpresence").child("chatrooms").child(mChatRoomId).child(mOUID).removeEventListener(useractivity);
        }
        if (pendingMsgs != null) {
            databaseReference.child("chatpresence").child("chatpendings").child(mOUID).removeEventListener(pendingMsgs);
        }

    }

    private void updateUserPendings() {

        databaseReference.child("chatpresence").child("chatpendings").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mMyIndicator = dataSnapshot.getValue(ChatIndicatorPojo.class);
                    int count = mMyIndicator.getmMsgCount()-howmanyread;
                    if(count<0){
                        count = 0;
                    }
                    mMyIndicator.setmMsgCount(count);
                    if(mMyIndicator.getmLastMsgUserImageUrl().equals(mOpponentImageUrl)){
                        mMyIndicator.setmLastMsgUserImageUrl("");
                    }
                    databaseReference.child("chatpresence").child("chatpendings").child(mAuth.getCurrentUser().getUid()).setValue(mMyIndicator).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Logger.v("self indicator updated");
                            howmanyread = 0;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setThumbnail(String mOpponentImageUrl) {
        Picasso.with(getApplicationContext())
                .load(mOpponentImageUrl+ "?width=100&width=100")
                .placeholder(ContextCompat.getDrawable(getApplicationContext()
                        , R.mipmap.ic_launcher_round))
                .transform(new CircleTransform())
                .fit()
                .into(mMainThumbnail, new Callback() {
                    @Override
                    public void onSuccess() {
                        Logger.v("image set: typiing thumb");
                    }

                    @Override
                    public void onError() {
                        Logger.v("image typing Error");
                    }


                });
        Picasso.with(getApplicationContext())
                .load(mOpponentImageUrl+ "?width=80&width=80")
                .placeholder(ContextCompat.getDrawable(getApplicationContext()
                        , R.mipmap.ic_launcher_round))
                .transform(new CircleTransform())
                .fit()
                .into(mTypingThumnail, new Callback() {
                    @Override
                    public void onSuccess() {
                        Logger.v("image set: typiing thumb");
                    }

                    @Override
                    public void onError() {
                        Logger.v("image typing Error");
                    }


                });
    }

    private void callNumber(String number) {

        InteractionPojo interactionPojo = new  InteractionPojo(preferenceManager.getUserId(),
                preferenceManager.getFuid(),
                preferenceManager.getRMN(),
                preferenceManager.getComapanyName(), preferenceManager.getDisplayName(),
                preferenceManager.getFcmToken(), mOUID,mOFUID,mORMN,mOpponentCompName,mOpponentCompName,mOpponentFcm);

        FirebaseFirestore.getInstance().collection("partners")
                .document(mOUID).collection("mCallsDump").document(getDateString())
                .collection("interactors").document(preferenceManager.getUserId()).set(interactionPojo);


        FirebaseFirestore.getInstance().collection("partners")
                .document(mOUID)
                .collection("mCalls").document(preferenceManager.getUserId()).set(interactionPojo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseFirestore.getInstance().collection("partners")
                        .document(preferenceManager.getUserId())
                        .collection("mCalls").document(mOUID).set(interactionPojo);
            }
        });

        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + Uri.encode(number.trim())));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent("z_chat_callopponent", bundle);

        RateReminderPojo rateReminderPojo =  new RateReminderPojo(mOpponentCompName,
                "mull",
                mORMN,
                mOUID,
                mOFUID,
                "call",
                new Date().getTime() + "",
                0.0,true);

        preferenceManager.setPrefRateReminder(new Gson().toJson(rateReminderPojo));
    }

    private String sendSMS(String mORMN) {

        try {
            // Construct data
            String apiKey = "apikey=" + "yourapiKey";
            String message = "&message=" + "This is your message";
            String sender = "&sender=" + "ILNCHA";
            String numbers = "&numbers=" + mORMN.substring(1);

            // Send data
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
            String data = apiKey + numbers + message + sender;
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
            conn.getOutputStream().write(data.getBytes("UTF-8"));
            final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                stringBuffer.append(line);
            }
            rd.close();

            return stringBuffer.toString();
        } catch (Exception e) {
            System.out.println("Error SMS " + e);
            return "Error " + e;
        }

    }

    /**
     * This method is use for checking internet connectivity
     * If there is no internet it will show an snackbar to user
     */
    private void internetCheck() {
        NoNet.monitor(this)
                .poll()
                .snackbar();
    }
}
