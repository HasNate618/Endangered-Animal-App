package com.nathanespejo.safeguardwildlife

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.nathanespejo.safeguardwildlife.API.DatabaseAPI
import com.nathanespejo.safeguardwildlife.Model.Animal
import com.nathanespejo.safeguardwildlife.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val FILE_NAME = "photo.jpg"
class SubmissionActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var selectBtn: Button
    lateinit var photoBtn: Button
    lateinit var submitBtn: Button
    lateinit var predictionText: TextView
    lateinit var dateText: TextView
    lateinit var locationText: TextView
    lateinit var imageView: ImageView
    lateinit var bitmap: Bitmap
    lateinit var imageProcessor: ImageProcessor
    lateinit var labels: List<String>
    lateinit var photoFile: File

    lateinit var animal: String
    lateinit var date: String
    lateinit var location: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_submission)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        selectBtn = findViewById(R.id.selectBtn)
        photoBtn = findViewById(R.id.photoBtn)
        submitBtn = findViewById(R.id.submitBtn)
        predictionText = findViewById(R.id.predictionText)
        dateText = findViewById(R.id.dateText)
        locationText = findViewById(R.id.locationText)
        imageView = findViewById(R.id.imageView)

        labels = application.assets.open("labels.txt").bufferedReader().readLines()

        //image processor
        imageProcessor = ImageProcessor.Builder()
            //.add(NormalizeOp(0.0f, 255.0f))
            .add(ResizeOp(96, 96, ResizeOp.ResizeMethod.BILINEAR))
            //.add(TransformToGrayscaleOp())
            .build()

        selectBtn.setOnClickListener{
            val intent: Intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent, 1)
        }
        photoBtn.setOnClickListener{
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)
            Log.d("LOGS", photoFile.absolutePath);

            val fileProvider = FileProvider.getUriForFile(this, "com.nathanespejo.safeguardwildlife.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            if (takePictureIntent.resolveActivity(this.packageManager) != null){
                startActivityForResult(takePictureIntent, 2)
            } else{
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }
        submitBtn.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java).also {
                //Add new animal to database
                if (this::animal.isInitialized){
                    val objSend = DatabaseAPI.Send()
                    val newAnimal = Animal(animal, date, location)
                    objSend.start(newAnimal)
                    Log.d("LOGS", "Animal added: " + newAnimal.toString())
                } else {
                    Log.d("LOGS", "No animal found")
                }

                startActivity(it)
            }
        }
    }

    private fun getPhotoFile(fileName: String): File {
        // Use 'getExternalFilesDir' on Context to access package-specific directories
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 2 && resultCode == Activity.RESULT_OK){
            //bitmap = data?.extras?.get("data") as Bitmap
            bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

            onImageUpdate()
        }

        if(requestCode == 1){
            val uri = data?.data;
            val photoPath = FileUtils.getMediaFilePathForN(uri, applicationContext)
            Log.d("LOGS", photoPath.toString());
            photoFile = File(photoPath.toString())
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            onImageUpdate()
        }
    }

    fun onImageUpdate(){
        setImageView()
        predictImage()
        getCreationDate()
        getLocation()
    }

    fun setImageView(){
        if (photoFile.exists()){
            val exif = ExifInterface(photoFile.absoluteFile.toString())
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val matrix = Matrix()

            when(orientation){
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
            }

            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0,0 , bitmap.width, bitmap.height, matrix, true)
            imageView.setImageBitmap(rotatedBitmap)
        }
    }

    // Creates a prediction for the bitmap in the Image View
    fun predictImage() {
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap);

        tensorImage = imageProcessor.process(tensorImage)

        val model = Model.newInstance(this)

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 96, 96, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        var maxIdx = 0
        outputFeature0.forEachIndexed{ index, fl ->
            if(outputFeature0[maxIdx] < fl){
                maxIdx = index
            }
        }

        val text = "Animal: " + labels[maxIdx]
        predictionText.setText(text)
        animal = labels[maxIdx]

        // Releases model resources if no longer used.
        model.close()
    }

    fun getCreationDate(){
        // Gets the EXIF data for the date the picture was taken
        // If there is no data, the current date is used
        val exif = ExifInterface(photoFile.absoluteFile.toString())

        val text: String
        if (exif.dateTime.toInt() == -1){
            val dtf = DateTimeFormatter.ofPattern("dd/MM/yyy")
            text = "Date: " + dtf.format(LocalDateTime.now())
            date = dtf.format(LocalDateTime.now())
        } else {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val stamp = Date(Timestamp(exif.dateTimeOriginal).time)
            text = "Date: " + sdf.format(stamp)
            date = sdf.format(stamp)
        }
        dateText.setText(text)
    }

    fun getLocation(){
        //Stores lat and long in location var
        val latLong = getLastKnownLocation()
        location = latLong[0].toString() + "," + latLong[1].toString()
        val text = "Location: $location"
        locationText.setText(text)
    }

    /*private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String? {
        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.getMaxAddressLineIndex()) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                Log.w("My Current loction address", strReturnedAddress.toString())
            } else {
                Log.w("My Current loction address", "No Address returned!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("My Current loction address", "Canont get Address!")
        }
        return strAdd
    }*/

    fun getLastKnownLocation():DoubleArray {
        val latLong = doubleArrayOf(0.00, 0.00)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            requestPermissions(permissions, 1)
            return latLong
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc: Location?->
                latLong[0] = loc?.latitude!!
                latLong[1] = loc?.longitude!!
            }
        return latLong
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteRecursive(File("data/user/0/com.nathanespejo.safeguardwildlife/files/"))
    }

    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }
}