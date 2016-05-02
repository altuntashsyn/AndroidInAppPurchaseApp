package com.altuntas.inapppurchase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.altuntas.inapppurchase.util.IabHelper;
import com.altuntas.inapppurchase.util.IabResult;
import com.altuntas.inapppurchase.util.Inventory;
import com.altuntas.inapppurchase.util.Purchase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG =
            "com.altuntas.inapppurchase.MainActivity";
    private Button clickBtn;
    private Button buyBtn;


    private IabHelper mIabHelper;
    private IabHelper.OnIabPurchaseFinishedListener mIabPurchaseFinishedListener;
    private IabHelper.QueryInventoryFinishedListener mQueryInventoryFinishedListener;
    private IabHelper.OnConsumeFinishedListener mOnConsumeFinishedListener;
    static final String ITEM_SKU = "android.test.purchased";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clickBtn = (Button) findViewById(R.id.button);
        buyBtn = (Button) findViewById(R.id.button2);
        clickBtn.setEnabled(false);

        settingPlayBilling();
        registerEvents();

    }

    private void settingPlayBilling() {
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkXLhgVWMmZoU6Lq+ai2ElQOWaQSlUxMq/hqCLe8I8/cKG/LJdMKKvVobjMj1j/00w5CPHXp9yNEk9Ossk79g3de18yviLp1wu+y/DXly7cyjAUu+0gs53f5g1DX1irrK0iNUOwn5/LwyA593c8MJf76BIKH3yrNjuOBXUeGmbkmXa+9HKVihQjPtamqb1IEljCgu46yZf2badz62P1qM7vjMeUwox6xBxBMTY/IZHh/A1fw2HwrMTK8/wnBvVf3ZhPdo5dSvZ5g0ISnoHv8wMke0Rt63/ivAzIzy9wpp89cJDIWOQQi2InSAghnFzV/C3WVNH1Hpn2aaaahZB5sdqQIDAQAB";
        mIabHelper = new IabHelper(this, base64EncodedPublicKey);

        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");
                    //consumeItem();
                }
            }
        });

        mIabPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isFailure()) {
                    return;
                } else if (purchase.getSku().equals(ITEM_SKU)) {
                    consumeItem();
                    buyBtn.setEnabled(false);

                }
            }
        };

        mQueryInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                if (result.isFailure()) {
                    // Handle failure
                } else {
                    mIabHelper.consumeAsync(inventory.getPurchase(ITEM_SKU), mOnConsumeFinishedListener);
                }

            }
        };

        mOnConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
            @Override
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                if (result.isSuccess()) {
                    clickBtn.setEnabled(true);
                } else {
                    // Handel Error
                }
            }
        };

    }

    private void consumeItem() {
        mIabHelper.queryInventoryAsync(mQueryInventoryFinishedListener);
    }

    private void buttonClicked() {
        clickBtn.setEnabled(false);
        buyBtn.setEnabled(true);
    }

    private void registerEvents() {
        clickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Click Event", Toast.LENGTH_SHORT).show();
            }
        });

        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Buy", Toast.LENGTH_SHORT).show();
                mIabHelper.launchSubscriptionPurchaseFlow(MainActivity.this, ITEM_SKU, 10001, mIabPurchaseFinishedListener, "mypurchasetoken");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mIabHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIabHelper != null) {
            mIabHelper.dispose();
            mIabHelper = null;
        }
    }
}
