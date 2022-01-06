/*
 * [y] hybris Platform
 *
 * Copyright (c) 2021 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package pl.polsl.km.mal.testData.data;


public class StreamDatabaseVariable
{
	private static boolean stream = false;

	public static void setStream(final boolean stream)
	{
		StreamDatabaseVariable.stream = stream;
	}

	public static boolean isStream()
	{
		return stream;
	}
}
