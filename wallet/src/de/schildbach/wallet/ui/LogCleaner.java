package de.schildbach.wallet.ui;

/**
 * Created by lavajumper on 11/14/2015.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.io.*;
import java.nio.channels.FileChannel;

import de.schildbach.wallet_sxc.R;

public class LogCleaner extends AlertDialog{

    public final Context context;
    public boolean ret;

    protected LogCleaner(Context context) {
        super(context);
        this.context = context;
    }

    public boolean showConfirmDialog(){

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        clearLogs();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        ret = false;
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.clear_logs_dialog_title);
        builder.setMessage(R.string.clear_logs_dialog_message).setPositiveButton(R.string.button_ok, dialogClickListener)
                .setNegativeButton(R.string.button_cancel, dialogClickListener).show();
        return ret;
    }

    public void clearLogs(){
        try
        {
            final File logDir = context.getDir("log", Context.MODE_PRIVATE);

            for (final File logFile : logDir.listFiles())
            {
                final String logFileName = logFile.getName();
                final File file;
                if (logFileName.endsWith(".log.gz"))
                    logFile.delete();
                else if (logFileName.endsWith(".log")) {
                    // This is probably the active log, we'll truncate it instead of deleting it.
                    FileChannel logChan = new FileOutputStream(logFile, true).getChannel();
                    logChan.truncate(0);
                    logChan.close();
                }

                else
                    continue;

            }
        }
        catch (final IOException x)
        {
            //Do nothing right now......
            //log.info("problem truncating logs", x);
        }

    }

}
