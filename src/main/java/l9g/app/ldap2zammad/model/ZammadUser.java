/*
 * Copyright 2023 Thorsten Ludewig (t.ludewig@gmail.com).
 *
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
 */
package l9g.app.ldap2zammad.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ZammadUser
{
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Integer id;

  private Integer organization_id;

  private String organization;

  private String login;

  private String firstname;

  private String lastname;

  private String email;

  private String password;

  /*
  private Object image;

  private Object image_source;
   */
  private String web;

  private String phone;

  private String fax;

  private String mobile;

  private String department;

  private Boolean vip;

  private Boolean verified;

  private Boolean active;

  private String note;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Date last_login;

  // @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String source;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Integer login_failed;

  private Boolean out_of_office;

  /*
  private Object out_of_office_start_at;

  private Object out_of_office_end_at;

  private Object out_of_office_replacement_id;
   */
  private ZammadPreferences preferences;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Integer updated_by_id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Integer created_by_id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Date created_at;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Date updated_at;
  
  // additional attributes
  private String customernumber;

  private List<String> roles;

  private List<Integer> role_ids;

  private List<Integer> organization_ids;

  private List<Integer> authorization_ids;

  private List<Integer> overview_sorting_ids;

  private Map<String, String[]> group_ids;
}
