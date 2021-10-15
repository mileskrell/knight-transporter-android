package edu.rutgers.knighttransporter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import edu.rutgers.knighttransporter.databinding.AlertViewHolderBinding
import edu.rutgers.knighttransporter.for_transloc.Alert
import java.text.SimpleDateFormat

class AlertsDialogFragment : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /**
         * It's okay that we don't provide the view root in this line, since we don't use any
         * important layout_* properties on the root of the inflated layout.
         *
         * The [SuppressLint] annotation above is for this line.
         */
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_alerts, null)

        val alerts = requireArguments().getParcelableArray(KEY_ALERTS)
            ?.filterIsInstance<Alert>()?.toList() ?: listOf()

        dialogView.findViewById<RecyclerView>(R.id.alerts).run {
            setHasFixedSize(true)
            adapter = AlertsAdapter(alerts)
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.alerts, alerts.size))
            .setView(dialogView)
            .setNeutralButton(R.string.close) { _, _ -> }
            .create()
    }

    class AlertsAdapter(private val alerts: List<Alert>) :
        RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AlertViewHolder(
            AlertViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
            holder.bind(alerts[position])
        }

        override fun getItemCount() = alerts.size

        class AlertViewHolder(private val binding: AlertViewHolderBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(alert: Alert) {
                binding.alertTitle.run {
                    text = alert.title
                    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    setOnClickListener {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(alert.guid)))
                    }
                }
                binding.alertPubDate.text = try {
                    val date = fromFormat.parse(alert.pubDate) ?: throw RuntimeException()
                    binding.root.context.getString(
                        R.string.posted,
                        toFormat.format(date)
                    )
                } catch (e: RuntimeException) {
                    alert.pubDate
                }
                // TODO: Don't use fromHtml, it looks bad
                binding.alertDescription.text = Html.fromHtml(alert.description)
            }
        }
    }

    companion object {
        const val KEY_ALERTS = "alerts"

        @SuppressLint("SimpleDateFormat")
        val fromFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

        @SuppressLint("SimpleDateFormat")
        val toFormat = SimpleDateFormat("EEE, MMM d, yyyy")

        @JvmStatic
        fun newInstance(alerts: List<Alert>) =
            AlertsDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArray(KEY_ALERTS, alerts.toTypedArray())
                }
            }
    }
}
