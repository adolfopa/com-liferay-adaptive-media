/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.adaptive.media.image.content.transformer.internal;

import com.liferay.adaptive.media.content.transformer.ContentTransformer;
import com.liferay.adaptive.media.content.transformer.ContentTransformerContentType;
import com.liferay.adaptive.media.content.transformer.constants.ContentTransformerContentTypes;
import com.liferay.adaptive.media.image.html.AdaptiveMediaImageHTMLTagFactory;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.model.FileEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(
	immediate = true, property = "content.transformer.content.type=html",
	service = ContentTransformer.class
)
public class HtmlContentTransformerImpl implements ContentTransformer<String> {

	@Override
	public ContentTransformerContentType<String> getContentType() {
		return ContentTransformerContentTypes.HTML;
	}

	@Override
	public String transform(String html) throws PortalException {
		if (html == null) {
			return null;
		}

		Document document = _parseDocument(html);

		for (Element image : document.select("img[data-fileEntryId]")) {
			long fileEntryId = Long.valueOf(image.attr("data-fileEntryId"));

			FileEntry fileEntry = _dlAppLocalService.getFileEntry(fileEntryId);

			String adaptiveTag = _adaptiveMediaImageHTMLTagFactory.create(
				image.toString(), fileEntry);

			image.replaceWith(_parseNode(adaptiveTag));
		}

		return document.body().html();
	}

	@Reference(unbind = "-")
	protected void setAdaptiveMediaImageHTMLTagFactory(
		AdaptiveMediaImageHTMLTagFactory adaptiveMediaImageHTMLTagFactory) {

		_adaptiveMediaImageHTMLTagFactory = adaptiveMediaImageHTMLTagFactory;
	}

	@Reference(unbind = "-")
	protected void setDLAppLocalService(DLAppLocalService dlAppLocalService) {
		_dlAppLocalService = dlAppLocalService;
	}

	private Document _parseDocument(String html) {
		Document.OutputSettings outputSettings = new Document.OutputSettings();

		outputSettings.prettyPrint(false);
		outputSettings.syntax(Document.OutputSettings.Syntax.xml);

		Document document = Jsoup.parseBodyFragment(html);

		document.outputSettings(outputSettings);

		return document;
	}

	private Element _parseNode(String tag) {
		Document document = _parseDocument(tag);

		Element body = document.body();

		return body.child(0);
	}

	private AdaptiveMediaImageHTMLTagFactory _adaptiveMediaImageHTMLTagFactory;
	private DLAppLocalService _dlAppLocalService;

}