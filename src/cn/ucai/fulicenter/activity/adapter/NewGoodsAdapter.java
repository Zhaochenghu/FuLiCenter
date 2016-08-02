package cn.ucai.fulicenter.activity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.fulibean.NewGoodBean;

/**
 * Created by Administrator on 2016/8/1.
 */
public class NewGoodsAdapter extends RecyclerView.Adapter<NewGoodsAdapter.NewGoodsHolder> {
    private Context mContext;
    private ArrayList<NewGoodBean> mList;

    public NewGoodsAdapter(Context mContext, ArrayList<NewGoodBean> list) {
        this.mContext = mContext;
        this.mList = list;
    }

    @Override

    public NewGoodsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NewGoodsHolder(View.inflate(mContext, R.layout.goods_adapter, null));
    }

    @Override
    public void onBindViewHolder(NewGoodsHolder holder, int position) {
        NewGoodBean newGoodBean = mList.get(position);

    }


    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    class NewGoodsHolder extends RecyclerView.ViewHolder {
        ImageView mIvGood;
        TextView mTvGoodName;
        TextView mTvPrice;

        public NewGoodsHolder(View itemView) {
            super(itemView);
            mIvGood = (ImageView) itemView.findViewById(R.id.iv_goods);
            mTvGoodName = (TextView) itemView.findViewById(R.id.tv_good_name);
            mTvPrice = (TextView) itemView.findViewById(R.id.tv_good_price);
        }
    }

}