package ru.handy.android.wm.learning;

import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.CountDownLatch;

import ru.handy.android.wm.R;
import ru.handy.android.wm.setting.Utils;

public class DialogLearning extends DialogFragment implements OnClickListener, Runnable {

    private CountDownLatch latch = null;
    private FragmentManager manager = null;
    private String tag = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.onActivityCreateSetTheme((AppCompatActivity) this.requireActivity());
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, 0);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        Bundle bundle = getArguments();
        adb.setTitle(bundle.getString("title", ""));
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setMessage(bundle.getString("message"));
        adb.setPositiveButton(R.string.ok, this);
        return adb.create();
    }

    @Override
    public void run() {
        show(manager, tag);
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public void setFragmentManager(FragmentManager manager) {
        this.manager = manager;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (latch != null) latch.countDown();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
