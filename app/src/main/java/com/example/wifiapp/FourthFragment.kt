package com.example.wifiapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.wifiapp.databinding.FragmentFourthBinding

/**
 * A simple [Fragment] subclass.
 * Use the [FourthFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

//Cell情報取得用フラグメント
class FourthFragment : Fragment() {

    private val TAG = "cellInfoFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var context: Context
        cellInfoToTV()
    }

    private fun cellInfoToTV(){
        getCellInformation { cellInfoList ->
            val sb = StringBuilder()
            val length = cellInfoList.size
            sb.append("取得Cell：${length}個")
            sb.append("\n")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                for (cellInfo in cellInfoList){
                    when(cellInfo){
                        is CellInfoLte ->{
                            Log.d(TAG,"CellInfoLTE")
                            val timeStamp = cellInfo.timeStamp
                            val cellID = cellInfo.cellIdentity.ci
                            val mccmnc = cellInfo.cellIdentity.mobileNetworkOperator
                            val EARFCN = cellInfo.cellIdentity.earfcn
                            val pci = cellInfo.cellIdentity.pci
                            val tac = cellInfo.cellIdentity.tac
                            val rsrp = cellInfo.cellSignalStrength.rsrp
                            val rsrq = cellInfo.cellSignalStrength.rsrq
                            val rssi = cellInfo.cellSignalStrength.rssi
                            val rssnr = cellInfo.cellSignalStrength.rssnr
                            val cqi = cellInfo.cellSignalStrength.cqi

                            sb.append("TimeStamp:${timeStamp}ns\n")
                            sb.append("CellType:LTE\n")
                            sb.append("CellID:$cellID\n")
                            sb.append("MCC+MNC:$mccmnc\n")
                            sb.append("CellType:LTE\n")
                            sb.append("EARFCN:$EARFCN\n")
                            sb.append("PCI:$pci\n")
                            sb.append("TAC:$tac\n")
                            sb.append("RSRP:$rsrp\n")
                            sb.append("RSSQ:$rsrq\n")
                            sb.append("RSSI:$rssi\n")
                            sb.append("RSSNR$rssnr\n")
                            sb.append("CQI:$cqi\n")
                            sb.append("------------------")
                            sb.append("\n")
                        }
                        is CellInfoNr ->{
                            Log.d(TAG,"CellInfoNR")
                            val timeStamp = cellInfo.timeStamp
                            val cellID = (cellInfo.cellIdentity as CellIdentityNr).nci
                            val mcc =(cellInfo.cellIdentity as CellIdentityNr).mccString
                            val mnc =(cellInfo.cellIdentity as CellIdentityNr).mncString
                            val NRARFCN =(cellInfo.cellIdentity as CellIdentityNr).nrarfcn
                            val pci =(cellInfo.cellIdentity as CellIdentityNr).pci
                            val tac =(cellInfo.cellIdentity as CellIdentityNr).tac
                            val ssRsrp =(cellInfo.cellSignalStrength as CellSignalStrengthNr).ssRsrp
                            val ssRsrq =(cellInfo.cellSignalStrength as CellSignalStrengthNr).ssRsrq
                            val ssSinr =(cellInfo.cellSignalStrength as CellSignalStrengthNr).ssSinr
                            val csiRsrp = (cellInfo.cellSignalStrength as CellSignalStrengthNr).csiRsrp
                            val csiRsrq =(cellInfo.cellSignalStrength as CellSignalStrengthNr).csiRsrq
                            val csiSinr =(cellInfo.cellSignalStrength as CellSignalStrengthNr).csiSinr

                            sb.append("TimeStamp:${timeStamp}ns\n")
                            sb.append("CellType:NR\n")
                            sb.append("NCI:$cellID\n")
                            sb.append("MCC+MNC:${mcc+mnc}\n")
                            sb.append("NRARFCN:$NRARFCN\n")
                            sb.append("PCI:$pci\n")
                            sb.append("TAC:$tac\n")
                            sb.append("(SS)RSRP:$ssRsrp\n")
                            sb.append("(SS)RSRQ:$ssRsrq\n")
                            sb.append("(SS)SINR:$ssSinr\n")
                            sb.append("(CSI)RSRP:$csiRsrp\n")
                            sb.append("(CSI)RSRQ:$csiRsrq\n")
                            sb.append("(CSI)SINR:$csiSinr\n")
                            sb.append("------------------")
                            sb.append("\n")
                        }
                    }
                }
            }
            else{
                for (cellInfo in cellInfoList){
                    when(cellInfo){
                        is CellInfoLte ->{
                            Log.d(TAG,"CellInfoLTE")
                            val timeStamp = cellInfo.timeStamp
                            val cellID = cellInfo.cellIdentity.ci
                            //          val mccmnc = cellInfo.cellIdentity.mobileNetworkOperator
                            val EARFCN = cellInfo.cellIdentity.earfcn
                            val pci = cellInfo.cellIdentity.pci
                            val tac = cellInfo.cellIdentity.tac
                            val rsrp = cellInfo.cellSignalStrength.rsrp
                            val rsrq = cellInfo.cellSignalStrength.rsrq
                            //          val rssi = cellInfo.cellSignalStrength.rssi
                            val rssnr = cellInfo.cellSignalStrength.rssnr
                            val cqi = cellInfo.cellSignalStrength.cqi

                            sb.append("TimeStamp:${timeStamp}ns\n")
                            sb.append("CellType:LTE\n")
                            sb.append("CellID:$cellID\n")
                            //        sb.append("MCC+MNC:$mccmnc\n")
                            sb.append("CellType:LTE\n")
                            sb.append("EARFCN:$EARFCN\n")
                            sb.append("PCI:$pci\n")
                            sb.append("TAC:$tac\n")
                            sb.append("RSRP:$rsrp\n")
                            sb.append("RSSQ:$rsrq\n")
                            //      sb.append("RSSI:$rssi\n")
                            sb.append("RSSNR$rssnr\n")
                            sb.append("CQI:$cqi\n")
                            sb.append("------------------")
                            sb.append("\n")
                        }
                    }
                }
            }

            var binding = FragmentFourthBinding.inflate(layoutInflater)
            binding.cellinfo.text = sb.toString()

        }
    }
    //Cellの取得
    private fun getCellInformation(result:(List<CellInfo>)-> Unit) {
        val telephonyManager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(requireContext(),"位置情報の権限がONではありません。",Toast.LENGTH_SHORT).show()
                return
            }
            telephonyManager.requestCellInfoUpdate(requireContext().mainExecutor, object : TelephonyManager.CellInfoCallback() {
                override fun onCellInfo(cellInfoList: MutableList<CellInfo>) {
                    result.invoke(cellInfoList)
                }
            })
        }else{
            result.invoke(telephonyManager.allCellInfo)
        }

    }

}