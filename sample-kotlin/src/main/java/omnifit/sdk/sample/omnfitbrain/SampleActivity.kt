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
    private val adapter: SampleAdapter by lazy {
        SampleAdapter()
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
            rcvDevice.adapter = adapter

            btnFind.setOnClickListener {
                viewModel.find(10) { throwable ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, throwable.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnStopFind.setOnClickListener {
                viewModel.stopFinding()
            }

            btnConnect.setOnClickListener {
                viewModel.connect { throwable ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, throwable.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnDisconnect.setOnClickListener {
                viewModel.disConnect()
            }

            btnFindConnect.setOnClickListener {
                viewModel.findWithConnect(5) { throwable ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, throwable.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnStopFindConnect.setOnClickListener {
                viewModel.stopFindingOrDisconnecting()
            }

            btnMeasure.setOnClickListener {
                viewModel.startMeasuring(measuringTime = 30, eyesState = Result.EyesState.CLOSED, onError = { throwable ->
                    runOnUiThread {
                        Toast.makeText(applicationContext, throwable.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                })
            }

            btnStopMeasure.setOnClickListener {
                viewModel.stopMeasuring()
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
                } else {
                    binding.tvScan.text = "Not Scan"
                }
            }

            isConnecting.observe(this@SampleActivity) {
                if (it) {
                    binding.tvConnect.text = "Connecting"
                } else {
                    binding.tvConnect.text = "Not Connect"
                }
            }

            isScanningOrConnecting.observe(this@SampleActivity) {
                if (it) {
                    binding.tvIsFindingIsConnecting.text = "Now State is finding or connecting"
                } else {
                    binding.tvIsFindingIsConnecting.text = "Waiting"
                }
            }

            isMeasuring.observe(this@SampleActivity) {
                if (it) {
                    binding.tvMeasure.text = "Measuring"
                } else {
                    binding.tvMeasure.text = "Not measuring"
                }
            }

            scannedBluetoothDevice.observe(this@SampleActivity) {
                binding.tvDevice.text = it?.let { "${it.name} - ${it.address}" } ?: "No Device"
            }

            scannedBluetoothDevices.observe(this@SampleActivity) {
                adapter.setData(it)
            }

            connectedBluetoothDevice.observe(this@SampleActivity) {
                binding.tvConnectedDevice.text = it?.let { "${it.name} - ${it.address}" } ?: "No Device"
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