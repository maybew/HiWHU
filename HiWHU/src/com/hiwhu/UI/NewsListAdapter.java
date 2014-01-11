package com.hiwhu.UI;

import java.util.List;

import com.hiwhu.tool.ImageLoader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NewsListAdapter extends ArrayAdapter<String[]> {
	private ListView mListView;
	private ImageLoader mImageLoader;

	// ViewHolder holder;
	// private Map<Integer, View> viewMap = new HashMap<Integer, View>();

	public NewsListAdapter(Activity activity, List<String[]> mList,
			ListView listView) {
		super(activity, 0, mList);
		mListView = listView;
		mImageLoader = new ImageLoader();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Activity activity = (Activity) getContext();

		View rowView = convertView;
		ViewCache viewCache;
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.info_list_item_layout, null);
			viewCache = new ViewCache(rowView);
			rowView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) rowView.getTag();
		}

		String[] mContent = this.getItem(position);

		if (mContent[4] == null || mContent[4].equals("")) {
			viewCache.getImageView().setVisibility(View.GONE);
		} else {
			String imageUrl = mContent[4];
			ImageView mImageView = viewCache.getImageView();
			mImageView.setTag(imageUrl);
			Bitmap cachedImage = mImageLoader.loadDrawable(imageUrl,
					new ImageLoader.ImageCallback() {

						@Override
						public void imageLoaded(Bitmap imageDrawable,
								String imageUrl) {
							// TODO Auto-generated method stub
							ImageView imageViewByTag = (ImageView) mListView
									.findViewWithTag(imageUrl);
							if (imageViewByTag != null) {
								imageViewByTag.setImageBitmap(imageDrawable);
							}

						}
					});
			mImageView.setImageBitmap(cachedImage);

			mImageView.setVisibility(View.VISIBLE);
		}

		viewCache.getTitle().setText(mContent[0]);
		viewCache.getDate().setText(mContent[1]);
		viewCache.getType().setText(mContent[2]);
		viewCache.getDesc().setText(mContent[3]);

		// viewCache.getTitle().getPaint().setFakeBoldText(true);

		return rowView;

	}

	public class ViewCache {

		private View baseView;
		private TextView titleView;
		private TextView descView;
		private TextView dateView;
		private TextView typeView;
		private ImageView imageView;

		public ViewCache(View baseView) {
			this.baseView = baseView;
		}

		public TextView getTitle() {
			if (titleView == null) {
				titleView = (TextView) baseView
						.findViewById(R.id.info_list_text_title);
			}
			return titleView;
		}

		public TextView getDesc() {
			if (descView == null) {
				descView = (TextView) baseView
						.findViewById(R.id.info_list_text_desc);
			}
			return descView;
		}

		public TextView getDate() {
			if (dateView == null) {
				dateView = (TextView) baseView
						.findViewById(R.id.info_list_text_date);
			}
			return dateView;
		}

		public TextView getType() {
			if (typeView == null) {
				typeView = (TextView) baseView
						.findViewById(R.id.info_list_text_type);
			}
			return typeView;
		}

		public ImageView getImageView() {
			if (imageView == null) {
				imageView = (ImageView) baseView
						.findViewById(R.id.info_list_image);
			}
			return imageView;
		}

	}

}
