/*
 * Copyright 2013-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.ui;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nullable;

import android.os.AsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import de.schildbach.wallet.Constants;
import de.schildbach.wallet.util.CrashReporter;
import de.schildbach.wallet_test.R;
import de.schildbach.wallet.util.Toast;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author Andreas Schildbach
 */
public abstract class ReportIssueDialogBuilder extends DialogBuilder implements OnClickListener {
    private final Activity activity;

    private EditText viewDescription;
    private CheckBox viewCollectDeviceInfo;
    private CheckBox viewCollectInstalledPackages;
    private CheckBox viewCollectApplicationLog;
    private CheckBox viewCollectWalletDump;
    private CheckBox viewDeliverViaEmail;
    private CheckBox viewSaveToDownload;

    private static final Logger log = LoggerFactory.getLogger(ReportIssueDialogBuilder.class);

    private class ReportContainer{
        public List<Uri> attachments;
        public CharSequence text;

        ReportContainer(List<Uri> _attachments, CharSequence _text){
            attachments = _attachments;
            text = _text;
        }

        public List<Uri> getAttachments(){ return attachments; }
        public CharSequence getText(){ return text; }
        public String getTextAsString() { return text.toString(); }
    }

    public ReportIssueDialogBuilder(final Activity activity, final int titleResId, final int messageResId) {
        super(activity);

        this.activity = activity;

        final LayoutInflater inflater = LayoutInflater.from(activity);
        final View view = inflater.inflate(R.layout.report_issue_dialog, null);

        ((TextView) view.findViewById(R.id.report_issue_dialog_message)).setText(messageResId);

        viewDescription = (EditText) view.findViewById(R.id.report_issue_dialog_description);

        viewCollectDeviceInfo = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_device_info);
        viewCollectInstalledPackages = (CheckBox) view
                .findViewById(R.id.report_issue_dialog_collect_installed_packages);
        viewCollectApplicationLog = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_application_log);
        viewCollectWalletDump = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_wallet_dump);
        viewDeliverViaEmail = (CheckBox) view.findViewById((R.id.report_issue_dialog_send_via_email));
        viewSaveToDownload = (CheckBox) view.findViewById((R.id.report_issue_dialog_save_to_downloads));

        setTitle(titleResId);
        setView(view);
        setPositiveButton(R.string.report_issue_dialog_report, this);
        setNegativeButton(R.string.button_cancel, null);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        final StringBuilder text = new StringBuilder();
        final List<Uri> attachments = new ArrayList<Uri>();
        final File cacheDir = activity.getCacheDir();
        final File reportDir = new File(cacheDir, "report");
        reportDir.mkdir();

        text.append(viewDescription.getText()).append('\n');

        try {
            final CharSequence contextualData = collectContextualData();
            if (contextualData != null) {
                text.append("\n\n\n=== contextual data ===\n\n");
                text.append(contextualData);
            }
        } catch (final IOException x) {
            text.append(x.toString()).append('\n');
        }

        try {
            text.append("\n\n\n=== application info ===\n\n");

            final CharSequence applicationInfo = collectApplicationInfo();

            text.append(applicationInfo);
        } catch (final IOException x) {
            text.append(x.toString()).append('\n');
        }

        try {
            final CharSequence stackTrace = collectStackTrace();

            if (stackTrace != null) {
                text.append("\n\n\n=== stack trace ===\n\n");
                text.append(stackTrace);
            }
        } catch (final IOException x) {
            text.append("\n\n\n=== stack trace ===\n\n");
            text.append(x.toString()).append('\n');
        }

        if (viewCollectDeviceInfo.isChecked()) {
            try {
                text.append("\n\n\n=== device info ===\n\n");

                final CharSequence deviceInfo = collectDeviceInfo();

                text.append(deviceInfo);
            } catch (final IOException x) {
                text.append(x.toString()).append('\n');
            }
        }

        if (viewCollectInstalledPackages.isChecked()) {
            try {
                text.append("\n\n\n=== installed packages ===\n\n");
                CrashReporter.appendInstalledPackages(text, activity);
            } catch (final IOException x) {
                text.append(x.toString()).append('\n');
            }
        }

        if (viewCollectApplicationLog.isChecked()) {
            final File logDir = new File(activity.getFilesDir(), "log");
            if (logDir.exists())
                for (final File logFile : logDir.listFiles())
                    if (logFile.isFile() && logFile.length() > 0)
                        attachments.add(FileProvider.getUriForFile(activity,
                                activity.getPackageName() + ".file_attachment", logFile));
        }

        if (viewCollectWalletDump.isChecked()) {
            try {
                final CharSequence walletDump = collectWalletDump();

                if (walletDump != null) {
                    final File file = File.createTempFile("wallet-dump.", ".txt", reportDir);

                    final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
                    writer.write(walletDump.toString());
                    writer.close();

                    attachments.add(
                            FileProvider.getUriForFile(activity, activity.getPackageName() + ".file_attachment", file));
                }
            } catch (final IOException x) {
                log.info("problem writing attachment", x);
            }
        }

        try {
            final File savedBackgroundTraces = File.createTempFile("background-traces.", ".txt", reportDir);
            if (CrashReporter.collectSavedBackgroundTraces(savedBackgroundTraces)) {
                attachments.add(FileProvider.getUriForFile(activity, activity.getPackageName() + ".file_attachment",
                        savedBackgroundTraces));
            }
            savedBackgroundTraces.deleteOnExit();
        } catch (final IOException x) {
            log.info("problem writing attachment", x);
        }

        text.append("\n\nPUT ADDITIONAL COMMENTS TO THE TOP. DOWN HERE NOBODY WILL NOTICE.");

        ReportContainer report = new ReportContainer(attachments,text);

        if(viewSaveToDownload.isChecked())
            saveToDownload(report);

        if(viewDeliverViaEmail.isChecked())
            startSend(subject(), text, attachments);

    }

    private void startSend(final String subject, final CharSequence text, final List<Uri> attachments) {
        final ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(activity);
        for (final Uri attachment : attachments)
            builder.addStream(attachment);
        builder.addEmailTo(Constants.REPORT_EMAIL);
        if (subject != null)
            builder.setSubject(subject);
        builder.setText(text);
        builder.setType("text/plain");

        builder.setChooserTitle(R.string.report_issue_dialog_mail_intent_chooser);
        builder.startChooser();
        log.info("invoked chooser for sending issue report");
    }

    private void saveToDownload(ReportContainer report){
        new WriteCrashReport().execute(report);
    }

    @Nullable
    protected abstract String subject();

    @Nullable
    protected CharSequence collectApplicationInfo() throws IOException {
        return null;
    }

    @Nullable
    protected CharSequence collectStackTrace() throws IOException {
        return null;
    }

    @Nullable
    protected CharSequence collectDeviceInfo() throws IOException {
        return null;
    }

    @Nullable
    protected CharSequence collectContextualData() throws IOException {
        return null;
    }

    @Nullable
    protected CharSequence collectWalletDump() throws IOException {
        return null;
    }

    private class WriteCrashReport extends AsyncTask<ReportContainer, Void, String>{
        protected String doInBackground(ReportContainer... reports){
            ReportContainer report=reports[0];
            List<Uri> attachments=report.attachments;
            CharSequence text = report.text;

            DateFormat df = new SimpleDateFormat("yyyyMMdd_HH_mm");
            String now = df.format(Calendar.getInstance().getTime());

            String outfilename = "SXC-CrashReport." + now + ".zip";
            String outpathname = Constants.Files.EXTERNAL_WALLET_BACKUP_DIR.getPath();

            File outputfile = new File( outpathname, outfilename);
            Writer allOut = null;

            final StringBuffer sb = new StringBuffer();
            sb.append(text + "\r\n");

            try {
                if (!outputfile.exists()) { outputfile.createNewFile(); }
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputfile));
                zos.putNextEntry(new ZipEntry("CrashReport.txt"));
                zos.write(sb.toString().getBytes());
                zos.closeEntry();

                for (final Uri attachment : attachments) {

                    InputStream istream = getContext().getContentResolver().openInputStream(attachment);
                    BufferedInputStream buff = new BufferedInputStream(istream);
                    zos.putNextEntry(new ZipEntry(attachment.getLastPathSegment()));
                    while(buff.available() > 0){
                        zos.write(buff.read());
                    }
                    zos.closeEntry();
                    istream.close();
                }
                zos.close();
            }catch(IOException e){
                log.error("Failed to copy attachments." + e.getMessage());
                return("FAIL:" + e.getMessage());
            }
            /*
            try{
                allOut = new OutputStreamWriter(new FileOutputStream(outputfile), Charsets.UTF_8);
                allOut.write(sb.toString());
                allOut.flush();
                allOut.close();
            }catch(IOException e){
                log.error("Couldn't write crash report to device: " + e.getMessage());
                log.error(sb.toString());
                return("FAIL:" + e.getMessage());
            }
            */
            log.warn("Crash Report successfully written: [--------------\r\n" + sb.toString() + "\r\n----------------]");
            return outfilename;

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            Toast toaster = new Toast(activity.getApplicationContext());
            toaster.toast(getContext().getResources().getText(R.string.report_issue_saving_to_local));
        }

        @Override
        protected void onPostExecute(String s) {
            Toast toaster = new Toast(activity.getApplicationContext());
            if(s.startsWith("FAIL")){
                String reason = getContext().getResources().getString(R.string.report_issue_fail_message) + "\r\n"
                        + s.substring(5);
                toaster.longToast(reason);
            } else {
                s = getContext().getResources().getString(R.string.report_issue_saved_message) + "\r\n" +
                        Constants.Files.EXTERNAL_WALLET_BACKUP_DIR.getPath() + "/" + s;
                toaster.longToast(s);
            }
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }

        @Override
        protected void onCancelled() {
            Toast toaster = new Toast(activity.getApplicationContext());
            CharSequence s = getContext().getResources().getString(R.string.report_issue_fail_message);
            toaster.longToast(s);
        }
    }
}
