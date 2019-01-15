package io.github.jacquelynoelle.padfoot.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import io.github.jacquelynoelle.padfoot.R;
import io.github.jacquelynoelle.padfoot.bluetoothle.BLEDeviceAdapter;
import io.github.jacquelynoelle.padfoot.bluetoothle.BLEService;

public class BLEScanActivity extends AppCompatActivity
        implements BLEDeviceAdapter.ListItemClickListener {

    private final static String TAG = BLEScanActivity.class.getSimpleName();
    private BLEDeviceAdapter mAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private boolean mScanning;
    private Handler mHandler;
    private RecyclerView mDeviceList;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blescan);
        getSupportActionBar().setTitle(getString(R.string.connect_tracker));

        mHandler = new Handler();
        mDeviceList = (RecyclerView) findViewById(R.id.rv_devices);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter and scanner
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDeviceList.setLayoutManager(layoutManager);
        mDeviceList.setHasFixedSize(true);

        mAdapter = new BLEDeviceAdapter( this);
        mDeviceList.setAdapter(mAdapter);

        scanLeDevice(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ble, menu);
        menu.findItem(R.id.menu_refresh).setVisible(true);
        menu.findItem(R.id.menu_stop).setVisible(true);
        menu.findItem(R.id.menu_home).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        TODO add scan in progress wheel?
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                mAdapter = new BLEDeviceAdapter(this);
                mDeviceList.setAdapter(mAdapter);
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case R.id.menu_home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mAdapter = new BLEDeviceAdapter( this);
        mDeviceList.setAdapter(mAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mAdapter.clear();
    }

    /**
     * This is where we receive our callback from
     * {@link BLEDeviceAdapter.ListItemClickListener}
     *
     * This callback is invoked when you click on an item in the list.
     *
     * @param clickedItemIndex Index in the list of the item that was clicked.
     */
    @Override
    public void onListItemClick(int clickedItemIndex) {
        final BluetoothDevice device = mAdapter.getDevice(clickedItemIndex);
        if (device == null) return;

        if (mScanning) {
            mBluetoothScanner.stopScan(mLeScanCallback);
            mScanning = false;
        }

        final Intent intent = new Intent(this, BLEService.class);
        intent.putExtra(EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(EXTRAS_DEVICE_ADDRESS, device.getAddress());
        startService(intent);

        final Intent returnToStepCount = new Intent(this, MainActivity.class);
        startActivity(returnToStepCount);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothScanner.stopScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothScanner.stopScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            mAdapter.addDevice(result.getDevice());
            super.onScanResult(callbackType, result);
        }
    };

}