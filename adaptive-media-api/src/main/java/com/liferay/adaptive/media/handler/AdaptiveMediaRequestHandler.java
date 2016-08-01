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

package com.liferay.adaptive.media.handler;

import com.liferay.adaptive.media.AdaptiveMedia;

import java.io.IOException;

import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Implementors of this interfaces will handle upcoming requests for a
 * particular kind of media (i.e. Media generated by one type of processor).
 *
 * @author Adolfo Pérez
 */
public interface AdaptiveMediaRequestHandler<T> {

	/**
	 * Return the requested {@link AdaptiveMedia} wrapped in an {@link
	 * Optional}. If no media if found matching the request, or an application
	 * error is raised while fetching the media, this method should return an
	 * empty {@link Optional}.
	 *
	 * @param request the request to process
	 *
	 * @return A non-null {@link Optional} containing the value (if any).
	 *
	 * @throws IOException if an IO error occurred while processing the
	 *         request.
	 * @throws ServletException if any other processing error occurred
	 */
	public Optional<AdaptiveMedia<T>> handleRequest(HttpServletRequest request)
		throws IOException, ServletException;

}