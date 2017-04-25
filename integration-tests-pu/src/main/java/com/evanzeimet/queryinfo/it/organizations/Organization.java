package com.evanzeimet.queryinfo.it.organizations;

import java.util.Date;
import java.util.List;

import com.evanzeimet.queryinfo.it.people.Person;

/*
 * #%L
 * queryinfo-integration-tests
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 Evan Zeimet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public interface Organization {

	Boolean getActive();

	void setActive(Boolean value);

	String getAddress1();

	void setAddress1(String address1);

	String getAddress2();

	void setAddress2(String address2);

	String getCity();

	void setCity(String city);

	Date getDateCreated();

	void setDateCreated(Date dateCreated);

	List<Person> getEmployees();

	void setEmployees(List<Person> employees);

	Long getId();

	void setId(Long id);

	String getName();

	void setName(String name);

	String getState();

	void setState(String state);

	Integer getYearFounded();

	void setYearFounded(Integer founded);

	String getZip();

	void setZip(String zip);

}
