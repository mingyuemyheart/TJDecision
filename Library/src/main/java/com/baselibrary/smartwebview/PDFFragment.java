package com.baselibrary.smartwebview;

import java.io.File;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.baselibrary.R;
import com.baselibrary.common.ColumnData;
import com.google.bitmapcache.ImageCache;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.PDFView.Configurator;
import com.joanzapata.pdfview.PdfOnlineLoader;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

public class PDFFragment extends Fragment implements OnPageChangeListener, OnLoadCompleteListener, OnClickListener {
	
	private PDFView pdfView;
	private ImageView preBtn;
	private ImageView nextBtn;
	private int curPage;
	private int totalPage;
	private ProgressBar progressBar = null;
	private ColumnData data = null;
	private String pdfUrl = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pdfview_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
		initPDFView(view);
	}

	private void initWidget(View view) {
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		preBtn = (ImageView) view.findViewById(R.id.pdf_pre_btn);
		preBtn.setOnClickListener(this);
		nextBtn = (ImageView) view.findViewById(R.id.pdf_next_btn);
		nextBtn.setOnClickListener(this);
		
		data = getArguments().getParcelable("data");
		if (data != null) {
			pdfUrl = data.dataUrl;
		}
		
	}
	
	private void initPDFView(View view) {
		pdfView = (PDFView) view.findViewById(R.id.pdf_view);
		pdfView.enableDoubletap(true);
		pdfView.enableSwipe(true);
		
		if (TextUtils.isEmpty(pdfUrl)) {
			// showToast(R.string.loading_fail);
			return;
		}
		final String cacheDir = ImageCache.getDiskCacheDir(getActivity(), "pdf").getAbsolutePath();
		final String cachePath = cacheDir + "/" + ImageCache.hashKeyForDisk(pdfUrl);
		PdfOnlineLoader loader = new PdfOnlineLoader(pdfUrl, cachePath) {
			@Override
			protected void onDownloadSuccess(String url) {
				Configurator conf = pdfView.fromFile(new File(cachePath));
				conf.onPageChange(PDFFragment.this);
				conf.onLoad(new OnLoadCompleteListener() {
					@Override
					public void loadComplete(int arg0) {
						progressBar.setVisibility(View.GONE);
					}
				});
				conf.load();
			}

			@Override
			protected void onDownloadProgress(int progress) {
			}

			@Override
			protected void onDownloadFailure(String url) {
			}

			@Override
			protected void onDownloadCancel(String url) {
			}
		};
		loader.start();
	}
	
	private void refreshPreAndNextBtn() {
		curPage = pdfView.getCurrentPage();
		totalPage = pdfView.getPageCount();
		if (totalPage <= 1) {
			preBtn.setVisibility(View.INVISIBLE);
			nextBtn.setVisibility(View.INVISIBLE);
		} else if (curPage == 0) {
			preBtn.setVisibility(View.INVISIBLE);
			nextBtn.setVisibility(View.VISIBLE);
		} else if (curPage == totalPage - 1) {
			preBtn.setVisibility(View.VISIBLE);
			nextBtn.setVisibility(View.INVISIBLE);
		} else {
			preBtn.setVisibility(View.VISIBLE);
			nextBtn.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void loadComplete(int nbPages) {
		refreshPreAndNextBtn();
	}

	@Override
	public void onPageChanged(int page, int pageCount) {
		curPage = pdfView.getCurrentPage();
		refreshPreAndNextBtn();
	}

	@Override
	public void onClick(View v) {
		curPage = pdfView.getCurrentPage();
		totalPage = pdfView.getPageCount();
		
		if (v.getId() == R.id.pdf_pre_btn) {
			if (curPage > 0) {
				pdfView.jumpTo(curPage);
			}
		}else if (v.getId() == R.id.pdf_next_btn) {
			if (curPage < totalPage) {
				pdfView.jumpTo(curPage + 2);
			}
		}

		refreshPreAndNextBtn();
	}
}
