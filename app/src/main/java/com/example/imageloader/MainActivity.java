package com.example.imageloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "MainActivity";
    public static long beforeTime;
    private static int counts;
    private static long afterTime;

    public ArrayList<String> mImgUrlList;
    private GridAdapter mGridAdapter;
    private GridView mGridView;
    public static boolean isGridIdle = true;
    public static TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.tv);
        mGridView = (GridView) findViewById(R.id.grid_view);
        initView();
        beforeTime = System.currentTimeMillis();
    }

    private void initView() {
        String[] imgUrls = {
                "http://ww3.sinaimg.cn/large/7a8aed7bjw1f2p0v9vwr5j20b70gswfi.jpg",
                "http://ww1.sinaimg.cn/large/7a8aed7bjw1f2nxxvgz7xj20hs0qognd.jpg",
                "http://ww2.sinaimg.cn/large/7a8aed7bjw1f2mteyftqqj20jg0siq6g.jpg",
                "http://ww2.sinaimg.cn/large/7a8aed7bjw1f2lkx2lhgfj20f00f0dhm.jpg",
                "http://ww3.sinaimg.cn/large/7a8aed7bjw1f2h04lir85j20fa0mx784.jpg",
                "http://ww3.sinaimg.cn/large/7a8aed7bjw1f2fuecji0lj20f009oab3.jpg",
                "http://ww1.sinaimg.cn/large/610dc034jw1f2ewruruvij20d70miadg.jpg",
                "http://ww3.sinaimg.cn/large/7a8aed7bjw1f2cfxa9joaj20f00fzwg2.jpg",
                "http://ww1.sinaimg.cn/large/610dc034gw1f2cf4ulmpzj20dw0kugn0.jpg",
                "http://ww1.sinaimg.cn/large/7a8aed7bjw1f27uhoko12j20ez0miq4p.jpg",
                "http://ww1.sinaimg.cn/large/7a8aed7bjw1f27uhoko12j20ez0miq4p.jpg",
                "http://ww2.sinaimg.cn/large/610dc034jw1f27tuwswd3j20hs0qoq6q.jpg",
                "http://ww3.sinaimg.cn/large/7a8aed7bjw1f26lox908uj20u018waov.jpg",
                "http://ww2.sinaimg.cn/large/7a8aed7bjw1f25gtggxqjj20f00b9tb5.jpg",
                "http://ww1.sinaimg.cn/large/7a8aed7bjw1f249fugof8j20hn0qogo4.jpg",
                "http://ww1.sinaimg.cn/large/7a8aed7bjw1f20ruz456sj20go0p0wi3.jpg",
                "http://ww4.sinaimg.cn/large/7a8aed7bjw1f1yjc38i9oj20hs0qoq6k.jpg",
                "http://ww3.sinaimg.cn/large/610dc034gw1f1yj0vc3ntj20e60jc0ua.jpg",
                "http://ww4.sinaimg.cn/large/7a8aed7bjw1f1xad7meu2j20dw0ku0vj.jpg",
                "http://ww1.sinaimg.cn/large/7a8aed7bjw1f1w5m7c9knj20go0p0ae4.jpg",
                "http://ww4.sinaimg.cn/large/7a8aed7bjw1f1so7l2u60j20zk1cy7g9.jpg",
                "http://ww4.sinaimg.cn/large/7a8aed7bjw1f1rmqzruylj20hs0qon14.jpg",
                "http://ww2.sinaimg.cn/large/7a8aed7bjw1f1qed6rs61j20ss0zkgrt.jpg",
                "http://ww3.sinaimg.cn/large/7a8aed7bjw1f1p77v97xpj20k00zkgpw.jpg",
                "http://ww1.sinaimg.cn/large/7a8aed7bjw1f1o75j517xj20u018iqnf.jpg",
                "http://ww4.sinaimg.cn/large/7a8aed7bjw1f1klhuc8w5j20d30h9gn8.jpg",
                "http://ww4.sinaimg.cn/large/7a8aed7bjw1f1jionqvz6j20hs0qoq7p.jpg",
                "http://ww3.sinaimg.cn/large/7a8aed7bjw1f1ia8qj5qbj20nd0zkmzp.jpg",
                "http://ww3.sinaimg.cn/large/7a8aed7bjw1f1h4f51wbcj20f00lddih.jpg",
                "http://ww1.sinaimg.cn/large/7a8aed7bjw1f1g2xpx9ehj20ez0mi0vc.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787534&di=258f1a7667f1250b1d3f49a835befd05&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201503%2F19%2F20150319215029_QAFK5.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787534&di=d7655ee83cd1fafd7671df18c33dab75&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201803%2F08%2F20180308221942_KuL8s.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787533&di=bd5ad9308a3ef90cf1ce6b18dae9b76c&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201801%2F28%2F20180128185226_eymxr.thumb.700_0.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787533&di=a6336e685cde28097f5240330efb0112&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201707%2F29%2F20170729215622_tTLBP.thumb.700_0.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787532&di=3de7405caf99e51d1d0d4aad907cb662&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201710%2F21%2F20171021212548_aWVAE.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787532&di=04df0265938c3119606e25d6058c20aa&imgtype=0&src=http%3A%2F%2Fimg5q.duitang.com%2Fuploads%2Fitem%2F201501%2F25%2F20150125024341_cm2ks.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787531&di=99f7dca1f6f3f7a60d027dbaf944c8ff&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201806%2F05%2F20180605193845_kmhss.thumb.700_0.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787531&di=af1d961ee211e03afbd5b4303b3cc57c&imgtype=0&src=http%3A%2F%2Fi0.hdslb.com%2Fbfs%2Farticle%2Ff9321b20c7cf75c1271dd992a8a01c84dbc216cf.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787530&di=6836b2b318338ee68e09527417dbebd2&imgtype=0&src=http%3A%2F%2Fpic.rmb.bdstatic.com%2Ff775f16cbda22b2fd7903480a06e44a0.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787529&di=b9957a2ad285ac1c2b5f1b7bb7c33bae&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201707%2F28%2F20170728135750_5tAWH.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787529&di=aa00a8a100c845645f996e0a334a52a3&imgtype=0&src=http%3A%2F%2Fpic.rmb.bdstatic.com%2Ffcd9555bd33f379035bcc05e71be30d2.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787528&di=a9da2bb0c277a34191a13ef3b9940a56&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201804%2F29%2F20180429210111_gtsnf.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787528&di=64a179b7ee5e9bb014f31e83405213d5&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201802%2F11%2F20180211150944_lbbff.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787523&di=1af1df691d835d2c0b808a09229f68a4&imgtype=0&src=http%3A%2F%2Fi0.hdslb.com%2Fbfs%2Farticle%2Fe134424943af8dc990779ecde24ec2a88acbfe66.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787522&di=957309af0934826ff7fcb201c243ab60&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201706%2F14%2F20170614193341_xBHrN.thumb.700_0.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787521&di=67715f90eff77695b93ff27f563eed5f&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fblog%2F201408%2F24%2F20140824144838_RrdtP.thumb.700_0.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787520&di=3c9a0c158b8db869041c2b2b29be0969&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201701%2F16%2F20170116105642_a3EXe.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787518&di=80c3a40988361571a93083272af27e5f&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201603%2F13%2F20160313195220_3rxfs.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787517&di=cc77bbdcbe4151a787cb1e3cee234fbb&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201507%2F22%2F20150722111147_2Sunw.jpeg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565967787515&di=c3b48a1c2befdd03cef574980e9da872&imgtype=0&src=http%3A%2F%2Fpic.rmb.bdstatic.com%2F2f93dafa2a197ccd64f6fc2d59cd36b2.jpeg"

        };
        mImgUrlList = new ArrayList<>();
        for (String s : imgUrls) {
            mImgUrlList.add(s);
        }

        mGridAdapter = new GridAdapter(this, mImgUrlList);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnScrollListener(this);

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            isGridIdle = true;
            mGridAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public static void afterLoad() {
        counts++;
        if (counts == 30) {
            afterTime = System.currentTimeMillis();
            int averTime = (int) ((afterTime - beforeTime) / 30);
            beforeTime = afterTime;
            textView.setText("平均加载时间: " + averTime);
        }
    }
}
