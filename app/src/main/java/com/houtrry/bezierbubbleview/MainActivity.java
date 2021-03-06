package com.houtrry.bezierbubbleview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author houtrry
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;

    private List<DataBean> mDatas = new ArrayList<>(100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        initRecycler();
    }

    private void initRecycler() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            mDatas.add(new DataBean("我是第 " + i + " 条数据", i + 1));
        }

        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(mDatas);
        mRecyclerView.setAdapter(recyclerAdapter);
    }


    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder> {

        private List<DataBean> mData;

        public RecyclerAdapter(List<DataBean> data) {
            mData = data;
        }

        @Override
        public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerHolder holder, final int position) {
            final DataBean dataBean = mData.get(position);
            Log.d(TAG, "onBindViewHolder: dataBean: " + dataBean);
            holder.tvContent.setText(dataBean.getContent());
            holder.bezierBubbleView.setTextValue(dataBean.getUnReadCount());
            holder.bezierBubbleView.setBezierBubbleListener(new BezierBubbleListener() {
                @Override
                public void dismissed(BezierBubbleView bezierBubbleView) {
                    dataBean.setUnReadCount(0);
                    Log.d(TAG, "dismissed: " + mData.get(position).getUnReadCount());
                }
            });
            holder.bezierBubbleView.setVisibility(dataBean.getUnReadCount() == 0 ? View.GONE : View.VISIBLE);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.bezierBubbleView.getLayoutParams();
            int rightMargin = layoutParams.rightMargin;
            Log.d(TAG, "onBindViewHolder: dataBean: " + dataBean+", rightMargin: "+rightMargin);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class RecyclerHolder extends RecyclerView.ViewHolder {

            private TextView tvContent;
            private BezierBubbleView bezierBubbleView;

            public RecyclerHolder(View itemView) {
                super(itemView);
                tvContent = (TextView) itemView.findViewById(R.id.tvContent);
                bezierBubbleView = (BezierBubbleView) itemView.findViewById(R.id.bezierBubbleView);
            }
        }
    }

    private class DataBean {
        private String content;
        private int unReadCount;

        public DataBean() {
        }

        public DataBean(String content, int unReadCount) {
            this.content = content;
            this.unReadCount = unReadCount;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getUnReadCount() {
            return unReadCount;
        }

        public void setUnReadCount(int unReadCount) {
            this.unReadCount = unReadCount;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "content='" + content + '\'' +
                    ", unReadCount=" + unReadCount +
                    '}';
        }
    }
}
