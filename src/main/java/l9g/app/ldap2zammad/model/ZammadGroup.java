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
public class ZammadGroup
{
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Integer id;

  private Integer signature_id;

  private Integer email_address_id;

  private String name;

  private Integer assignment_timeout;

  private String follow_up_possible;

  private Integer reopen_time_in_days;

  private Boolean follow_up_assignment;

  private Boolean active;

  private Boolean shared_drafts;

  private String note;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Integer updated_by_id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Integer created_by_id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Date created_at;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Date updated_at;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<Integer> user_ids;
}
