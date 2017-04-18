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

package com.liferay.adaptive.media.image.web.html;

import com.liferay.adaptive.media.AdaptiveMediaException;
import com.liferay.adaptive.media.image.mediaquery.Condition;
import com.liferay.adaptive.media.image.mediaquery.MediaQuery;
import com.liferay.adaptive.media.image.mediaquery.MediaQueryProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.StringBundler;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Alejandro Tardín
 */
@RunWith(MockitoJUnitRunner.class)
public class AdaptiveMediaImageHTMLTagFactoryImplTest {

	@Before
	public void setUp() throws AdaptiveMediaException, PortalException {
		_adaptiveMediaImageHTMLTagFactory.setMediaQueryProvider(
			_mediaQueryProvider);
	}

	@Test
	public void testCreatesAPictureTag() throws Exception {
		_addMediaQueries(_createMediaQuery(1989, "adaptiveURL"));

		StringBundler expectedSB = new StringBundler(5);

		expectedSB.append("<picture data-fileEntryId=\"0\">");
		expectedSB.append("<source media=\"(max-width:1989px)\" ");
		expectedSB.append("srcset=\"adaptiveURL\"/>");
		expectedSB.append(
			"<img src=\"originalURL\" data-fileEntryId=\"1234\"/>");
		expectedSB.append("</picture>");

		String originalImgTag =
			"<img src=\"originalURL\" data-fileEntryId=\"1234\"/>";

		String pictureTag = _adaptiveMediaImageHTMLTagFactory.create(
			originalImgTag, _fileEntry);

		Assert.assertEquals(expectedSB.toString(), pictureTag);
	}

	@Test
	public void testCreatesAPictureTagWithSeveralMediaQueries()
		throws Exception {

		_addMediaQueries(
			_createMediaQuery(1986, "adaptiveURL1"),
			_createMediaQuery(1989, "adaptiveURL2"));

		StringBundler expectedSB = new StringBundler(8);

		expectedSB.append("<picture data-fileEntryId=\"0\">");
		expectedSB.append("<source media=\"(max-width:1986px)\" ");
		expectedSB.append("srcset=\"adaptiveURL1\"/>");
		expectedSB.append("<source media=\"(max-width:1989px)\" ");
		expectedSB.append("srcset=\"adaptiveURL2\"/>");
		expectedSB.append(
			"<img src=\"originalURL\" data-fileEntryId=\"1234\"/>");
		expectedSB.append("</picture>");

		String originalImgTag =
			"<img src=\"originalURL\" data-fileEntryId=\"1234\"/>";

		String pictureTag = _adaptiveMediaImageHTMLTagFactory.create(
			originalImgTag, _fileEntry);

		Assert.assertEquals(expectedSB.toString(), pictureTag);
	}

	@Test
	public void testReturnsTheOriginalImgTagIfThereAreNoMediaQueries()
		throws Exception {

		_addMediaQueries();

		String originalImgTag =
			"<img src=\"originalURL\" data-fileEntryId=\"1234\"/>";

		String pictureTag = _adaptiveMediaImageHTMLTagFactory.create(
			originalImgTag, _fileEntry);

		Assert.assertSame(originalImgTag, pictureTag);
	}

	@Test
	public void testSupportsImageTagsWithNewLineCharacters() throws Exception {
		_addMediaQueries(_createMediaQuery(1989, "adaptiveURL"));

		StringBundler originalSB = new StringBundler(3);

		originalSB.append("<img data-fileEntryId=\"1234\" ");
		originalSB.append(CharPool.NEW_LINE);
		originalSB.append("src=\"adaptable\"/>");

		StringBundler expectedSB = new StringBundler(6);

		expectedSB.append("<picture data-fileEntryId=\"0\">");
		expectedSB.append("<source media=\"(max-width:1989px)\" ");
		expectedSB.append(
			"srcset=\"adaptiveURL\"/><img data-fileEntryId=\"1234\" ");
		expectedSB.append(CharPool.NEW_LINE);
		expectedSB.append("src=\"adaptable\"/>");
		expectedSB.append("</picture>");

		String pictureTag = _adaptiveMediaImageHTMLTagFactory.create(
			originalSB.toString(), _fileEntry);

		Assert.assertEquals(expectedSB.toString(), pictureTag);
	}

	@Test
	public void testTheAttributeIsCaseInsensitive() throws Exception {
		_addMediaQueries(_createMediaQuery(1989, "adaptiveURL"));

		StringBundler expectedSB = new StringBundler(5);

		expectedSB.append("<picture data-fileEntryId=\"0\">");
		expectedSB.append("<source media=\"(max-width:1989px)\" ");
		expectedSB.append("srcset=\"adaptiveURL\"/>");
		expectedSB.append(
			"<img src=\"originalURL\" datA-fileENTryID=\"1234\"/>");
		expectedSB.append("</picture>");

		String originalImgTag =
			"<img src=\"originalURL\" datA-fileENTryID=\"1234\"/>";

		String pictureTag = _adaptiveMediaImageHTMLTagFactory.create(
			originalImgTag, _fileEntry);

		Assert.assertEquals(expectedSB.toString(), pictureTag);
	}

	private void _addMediaQueries(MediaQuery... mediaQueries) throws Exception {
		Mockito.when(
			_mediaQueryProvider.getMediaQueries(_fileEntry)
		).thenReturn(
			Arrays.asList(mediaQueries)
		);
	}

	private MediaQuery _createMediaQuery(final int width, String url)
		throws Exception {

		return new MediaQuery(
			Arrays.asList(new Condition("max-width", width + "px")), url);
	}

	private final AdaptiveMediaImageHTMLTagFactoryImpl
		_adaptiveMediaImageHTMLTagFactory =
			new AdaptiveMediaImageHTMLTagFactoryImpl();

	@Mock
	private FileEntry _fileEntry;

	@Mock
	private MediaQueryProvider _mediaQueryProvider;

}