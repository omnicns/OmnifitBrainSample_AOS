package omnifit.sdk.sample.omnfitbrain

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import omnifit.sdk.sample.omnfitbrain.databinding.ItemDeviceBinding

@SuppressLint("MissingPermission")
class SampleAdapter: RecyclerView.Adapter<SampleAdapter.ViewHolder>() {
    private var itemList: List<BluetoothDevice?> = emptyList()

    fun setData(list: List<BluetoothDevice?>) {
        itemList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemDeviceBinding>(LayoutInflater.from(parent.context), R.layout.item_device, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    inner class ViewHolder(private val binding: ItemDeviceBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BluetoothDevice?) {
            binding.apply {
                tvDeviceName.text = item?.name
                tvDeviceAddress.text = item?.address
            }
        }
    }
}