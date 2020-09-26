package cn.yue.base.framesequence;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import cn.yue.base.frame.GlideApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.image);
        GlideApp.with(this)
                .asFrame()
                .load("https://images.ypcang.com/1601091463188.gif")
                .into(imageView);
    }
}