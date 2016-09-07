package com.wolkabout.hexiwear.util;

import android.app.*;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.*;
import android.widget.Toast;

import com.wolkabout.hexiwear.R;
import com.wolkabout.hexiwear.activity.ReadingsActivity_;

/**
 * Created by asad on 9/7/16.
 */
public class DeviceClickChoice extends  DialogFragment{

    private BluetoothDevice BtDevice;

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        CharSequence items[] = {getActivity().getString(R.string.telemetry_title), getActivity().getString(R.string.readout_title)};

        builder.setTitle(getActivity().getString(R.string.tap_action_title))
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (BtDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                            switch (which) {
                                case 0:
                                    break;
                                case 1:
                                    ReadingsActivity_.intent(getActivity()).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).device(BtDevice).start();
                                    break;
                            }
                        }else{
                            Toast.makeText(getActivity(), R.string.unbounded_device_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        return builder.create();
    }

    public void setDevice(BluetoothDevice device) {
        this.BtDevice = device;
    }
}
