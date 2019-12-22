package ru.hbracket.sbmvi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import ru.hbracket.sbmvi.mvi.*

class MainActivity : AppCompatActivity() {

    private val feature = RxFeature(
        ChatState("egor", "", Messages.Loading),
        setOf(ChatEffect.SubToChat, ChatEffect.LoadHistory),
        ChatState::update,
        ChatHandler()
    )
    private var state: ChatState = ChatState("egor", "", Messages.Loading)
    private var stateSubscription: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_send.setOnClickListener {
            feature.mutate(ChatMutation.SendMessage)
        }

        user_message_et.doOnTextChanged { text, _, _, _ ->
            feature.mutate(ChatMutation.NewInput(text as String))
        }
    }

    override fun onResume() {
        super.onResume()
        stateSubscription = feature.state()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                render(state)
                this.state = state
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("chat_state", state)
    }

    override fun onPause() {
        super.onPause()
        stateSubscription?.dispose()
        stateSubscription = null
    }

    private fun render(state: ChatState) {
        state.messages // TODO put into recycler
        user_message_et.setText(state.input)
    }


}
