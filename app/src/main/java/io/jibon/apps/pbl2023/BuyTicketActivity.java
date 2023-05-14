package io.jibon.apps.pbl2023;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Date;

public class BuyTicketActivity extends AppCompatActivity {
    public Activity activity;
    RelativeLayout loaderView;
    public String event_id = "";
    public CustomTools customTools;
    public TextView eventTitle, eventPhoneEmail, eventTxnNotice, errorPage;
    public  EditText ticketBuyerName, ticketBuyerNID, ticketBuyerTXN, ticketBuyerEmail, ticketBuyerTXNAmount;
    public ImageView eventLogo, yourImageView, yourIdCardImageView;
    public Button pickYourFromGallery, pickIdCardFromGallery, buyTicketBtn;
    int PICK_IMAGE_AVATAR = 449; // for avatar
    int PICK_IMAGE_ID_CARD = 450; // for nid card
    public Bitmap yourImageBitmap, yourIdCardImageBitmap;
    public Boolean requiredTxnId = true;
    public ScrollView eventTicketBuyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_buy_ticket);

        try {
            // define var
            customTools = new CustomTools(activity);

            //find views
            loaderView = activity.findViewById(R.id.loaderView);
            eventTitle = activity.findViewById(R.id.eventTitle);
            eventLogo = activity.findViewById(R.id.eventLogo);
            eventTicketBuyView = activity.findViewById(R.id.eventTicketBuyView);
            eventTxnNotice = activity.findViewById(R.id.eventTxnNotice);
            eventPhoneEmail = activity.findViewById(R.id.eventPhoneEmail);
            pickYourFromGallery = activity.findViewById(R.id.pickYourFromGallery);
            pickIdCardFromGallery = activity.findViewById(R.id.pickIdCardFromGallery);
            yourImageView = activity.findViewById(R.id.yourImageView);
            yourIdCardImageView = activity.findViewById(R.id.yourIdCardImageView);
            ticketBuyerEmail = activity.findViewById(R.id.ticketBuyerEmail);
            buyTicketBtn = activity.findViewById(R.id.buyTicketBtn);
            errorPage = activity.findViewById(R.id.errorPage);

            ticketBuyerName  = activity.findViewById(R.id.ticketBuyerName);
            ticketBuyerNID = activity.findViewById(R.id.ticketBuyerNID);
            ticketBuyerTXN = activity.findViewById(R.id.ticketBuyerTXN);
            ticketBuyerTXNAmount = activity.findViewById(R.id.ticketBuyerTXNAmount);

            buyTicketBtn.setOnClickListener(v -> {
                boolean allOkay = true;
                ticketBuyerName.setText(String.valueOf(ticketBuyerName.getText()).replaceAll("[^a-zA-Z\\s]", ""));
                ticketBuyerNID.setText(String.valueOf(ticketBuyerNID.getText()).replaceAll("[^0-9a-zA-Z]", ""));

                if (ticketBuyerName.getText().length() < 3 ){
                    ticketBuyerName.setError("Enter Your Valid Name as per NID");
                    allOkay = false;
                }
                if (ticketBuyerEmail.getText().length() < 3 || !EmailValidator.isValidEmail(String.valueOf(ticketBuyerEmail.getText()))){
                    ticketBuyerEmail.setError("Enter a valid Email");
                    allOkay = false;
                }
                if (ticketBuyerNID.getText().length() < 10 ){
                    ticketBuyerNID.setError("Enter your NID card or Passport number correctly");
                    allOkay = false;
                }
                if (ticketBuyerTXN.getText().length() < 3 && requiredTxnId){
                    ticketBuyerTXN.setError("Transaction ID is required!");
                    allOkay = false;
                }
                if (ticketBuyerTXNAmount.getText().length() < 1 && requiredTxnId){
                    ticketBuyerTXNAmount.setError("Transaction amount is required!");
                    allOkay = false;
                }
                if (yourImageBitmap == null){
                    pickYourFromGallery.setError("Please pick a valid image");
                    allOkay = false;
                }
                if (yourIdCardImageBitmap == null){
                    pickIdCardFromGallery.setError("Please pick a valid image");
                    allOkay = false;
                }
                if (allOkay){
                    String url = CustomTools.URL+"/json.php";
                    loaderView.setVisibility(View.VISIBLE);
                    BuyTicketSubmit buyTicketSubmit = new BuyTicketSubmit(activity, url, ticketBuyerName.getText().toString(), ticketBuyerEmail.getText().toString(), ticketBuyerNID.getText().toString(), ticketBuyerTXN.getText().toString(), ticketBuyerTXNAmount.getText().toString(), yourImageBitmap, yourIdCardImageBitmap, event_id, ((code, result) -> {
                        loaderView.setVisibility(View.GONE);
                        try {
                            if (result.has("buy_ticket")) {
                                if (result.getBoolean("buy_ticket")) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setTitle("In Review!")
                                            .setMessage("Your request has been submitted to event owner.\nYou will be notified of status with an email.")
                                            .setPositiveButton("Exit", (dialog, which) -> {
                                                dialog.cancel();
                                                activity.finish();
                                            })
                                            .setCancelable(false);
                                    builder.create().show();
                                } else {
                                    customTools.alert("Error!",result.getString("buy_ticket_error"));
                                }
                            }
                            Log.e("errnos res", String.valueOf(result));
                        }catch (Exception e){
                            Log.e("errnos error", String.valueOf(result));
                        }
                    }));
                    buyTicketSubmit.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });

            Bundle bundle = activity.getIntent().getExtras();
            if (bundle != null) {
                loaderView.setVisibility(View.VISIBLE);
                event_id = bundle.getString("event_id");
                String event_details_url = CustomTools.URL + "/event/" + event_id;
                @SuppressLint("SetTextI18n") Internet2 internet2 = new Internet2(activity, event_details_url, ((code, result) -> {
                    try {
                        if (code == 200 && result.has("id")) {
                            loaderView.setVisibility(View.GONE);


                            findViewById(R.id.openEventVerifier).setOnClickListener(v -> {
                                try {
                                    Intent intent = new Intent(activity, TicketVerifierActivity.class);
                                    intent.putExtra("app_code", result.getString("app_code"));
                                    intent.putExtra("event_id", result.getString("id"));
                                    activity.startActivity(intent);
                                }catch (Exception e){}
                            });


                            int time = (int) ((new Date()).getTime()/1000);

                            if (Float.parseFloat(result.getString("sell_start_time")) < time && Float.parseFloat(result.getString("sell_end_time")) >  time){
                                errorPage.setVisibility(View.GONE);
                                eventTicketBuyView.setVisibility(View.VISIBLE);
                            }else if(Float.parseFloat(result.getString("sell_end_time")) < time){
                                errorPage.setText("Ticket sell is already end!");
                            }else if (Float.parseFloat(result.getString("sell_start_time")) > time){
                                errorPage.setText("Ticket Sell is not started yet!");
                            }

                            if(Float.parseFloat(result.getString("event_time")) < time){
                                errorPage.setText("Event is Running!");
                            }

                            eventTitle.setText(result.getString("title"));
                            Glide.with(activity)
                                    .load(CustomTools.URL+"/"+result.getString("event_logo"))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(eventLogo);
                            eventPhoneEmail.setText("("+result.getString("contact_number")+", "+result.getString("contact_email")+")");

                            pickYourFromGallery.setOnClickListener(this::pickYourFromGallery);
                            pickIdCardFromGallery.setOnClickListener(this::pickIdCardFromGallery);

                            if (!result.getString("payment_method").equalsIgnoreCase("free")){
                                eventTxnNotice.setText(result.getString("payment_notice"));
                                eventTxnNotice.setVisibility(View.VISIBLE);
                                ticketBuyerTXN.setVisibility(View.VISIBLE);
                                ticketBuyerTXNAmount.setVisibility(View.VISIBLE);
                            }else{
                                requiredTxnId = false;
                            }

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("Sorry!")
                                    .setMessage("Event is not available!")
                                    .setPositiveButton("Exit", (dialog, which) -> {
                                        dialog.cancel();
                                        activity.finish();
                                    })
                                    .setCancelable(false);
                            builder.create().show();
                            return;
                        }
                    }catch (Exception e){
                        Log.e("errnos", e.getMessage());
                    }
                }));
                internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }catch (Exception e){
            Log.e("errnos", e.getMessage());
        }
    }


    public void pickYourFromGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_AVATAR);
    }

    public void yourImagePicked(Bitmap bitmap){
        yourImageBitmap = scaleDownBitmap(bitmap, 1024);
        yourImageView.setImageBitmap(yourImageBitmap);
        yourImageView.setVisibility(View.VISIBLE);
        yourImageView.setOnClickListener(this::pickYourFromGallery);
        pickYourFromGallery.setVisibility(View.GONE);
    }

    public void pickIdCardFromGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_ID_CARD);
    }

    public void idCardImagePicked(Bitmap bitmap){
        yourIdCardImageBitmap = scaleDownBitmap(bitmap, 1024);;
        yourIdCardImageView.setImageBitmap(yourIdCardImageBitmap);
        yourIdCardImageView.setVisibility(View.VISIBLE);
        yourIdCardImageView.setOnClickListener(this::pickIdCardFromGallery);
        pickIdCardFromGallery.setVisibility(View.GONE);
    }

    public static Bitmap scaleDownBitmap(Bitmap bitmap, int newWidth) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newHeight = (int) ((float) originalHeight / ((float) originalWidth / (float) newWidth));
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                yourImagePicked(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
            } catch (Exception e) {
                Log.e("errnos", e.getMessage());
            }
        }
        if (requestCode == PICK_IMAGE_ID_CARD && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                idCardImagePicked(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
            } catch (Exception e) {
                Log.e("errnos", e.getMessage());
            }
        }
    }


}