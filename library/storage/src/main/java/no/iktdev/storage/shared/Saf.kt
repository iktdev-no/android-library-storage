package no.iktdev.storage.shared

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileOutputStream
import java.lang.RuntimeException

open class Saf(open val context: Context, open val safRequestCode: Int = 1) {


    fun safRequest(activity: Activity, fileName: String, dataType: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = dataType
        intent.putExtra(Intent.EXTRA_TITLE, fileName)

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);


        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        activity.startActivityForResult(intent, safRequestCode)
    }

}