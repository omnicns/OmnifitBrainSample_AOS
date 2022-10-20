package omnifit.sdk.sample.omnfitbrain

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import omnifit.sdk.omnifitbrain.OCWH20ViewModel
import omnifit.sdk.omnifitbrain.model.Result
import omnifit.sdk.sample.omnfitbrain.databinding.ActivitySampleBinding

class SampleActivity : AppCompatActivity() {
    private val viewModel: OCWH20ViewModel by viewModels()
    private val binding: ActivitySampleBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_sample)
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                100
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                100
            )
        }

        binding.apply {
            lifecycleOwner = this@SampleActivity

            btnScan.setOnClickListener {
                viewModel.find(10) { throwable ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, throwable.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnConnect.setOnClickListener {
                viewModel.connect { throwable ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, throwable.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnScanConnect.setOnClickListener {
                viewModel.findWithConnect(10) { throwable ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, throwable.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnMeasure.setOnClickListener {
                viewModel.startMeasuring(measuringTime = 30, eyesState = Result.EyesState.CLOSED, onError = { throwable ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, throwable.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                })
            }

            btnGetSerial.setOnClickListener {
                viewModel.readSerialNo { serialNum ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, serialNum, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnGetSignal.setOnClickListener {
                viewModel.readSignalStability { signalStability ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, signalStability, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnGetStart.setOnClickListener {
                viewModel.readMeasureStartChangeTime { time ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, time, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.apply {
            isScanning.observe(this@SampleActivity) {
                if (it) {
                    binding.tvScan.text = "Scanning"
                    binding.btnScan.text = "Stop scan"
                } else {
                    binding.tvScan.text = "Not Scan"
                    binding.btnScan.text = "Start scan"
                }
            }

            isConnecting.observe(this@SampleActivity) {
                if (it) {
                    binding.tvConnect.text = "Connecting"
                    binding.btnConnect.text = "Disconnect"
                } else {
                    binding.tvConnect.text = "Not Connect"
                    binding.btnConnect.text = "Start connect"
                }
            }

            isScanningOrConnecting.observe(this@SampleActivity) {
                if (it) {
                    binding.btnScanConnect.text = "Stop scanning or disconnecting"
                } else {
                    binding.btnScanConnect.text = "Waiting"
                }
            }

            isMeasuring.observe(this@SampleActivity) {
                if (it) {
                    binding.tvMeasure.text = "Measuring"
                    binding.btnMeasure.text = "Stop measuring"
                } else {
                    binding.tvMeasure.text = "Not measuring"
                    binding.btnMeasure.text = "Start measuring"
                }
            }

            scannedBluetoothDevice.observe(this@SampleActivity) {
                binding.tvDevice.text = it?.name ?: "No device"
            }

            // Device status monitoring part(electrodeStatus + eegStabilityValue + batteryLevel)
            headSetInfo.observe(this@SampleActivity) {
                binding.tvStatus.text = it
            }

            result.observe(this@SampleActivity) {
                binding.tvResult.text = it.toString()
            }
        }
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}