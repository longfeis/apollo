package com.ctrip.framework.apollo.portal.api;


import com.google.common.base.Joiner;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.AppNamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.dto.CommitDTO;
import com.ctrip.framework.apollo.common.dto.InstanceDTO;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.core.enums.Env;

import org.springframework.boot.actuate.health.Health;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


@Service
public class AdminServiceAPI {

  @Service
  public static class HealthAPI extends API {

    public Health health(Env env) {
      return restTemplate.get(env, "/health", Health.class);
    }
  }

  @Service
  public static class AppAPI extends API {

    public AppDTO loadApp(Env env, String appId) {
      return restTemplate.get(env, "apps/{appId}", AppDTO.class, appId);
    }

    public AppDTO createApp(Env env, AppDTO app) {
      return restTemplate.post(env, "apps", app, AppDTO.class);
    }
  }


  @Service
  public static class NamespaceAPI extends API {

    public List<NamespaceDTO> findNamespaceByCluster(String appId, Env env, String clusterName) {
      NamespaceDTO[] namespaceDTOs = restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces",
                                                      NamespaceDTO[].class, appId,
                                                      clusterName);
      return Arrays.asList(namespaceDTOs);
    }

    public NamespaceDTO loadNamespace(String appId, Env env, String clusterName,
                                      String namespaceName) {
      NamespaceDTO dto =
          restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}",
                           NamespaceDTO.class, appId, clusterName, namespaceName);
      return dto;
    }


    public NamespaceDTO createNamespace(Env env, NamespaceDTO namespace) {
      return restTemplate
          .post(env, "apps/{appId}/clusters/{clusterName}/namespaces", namespace, NamespaceDTO.class,
                namespace.getAppId(), namespace.getClusterName());
    }

    public AppNamespaceDTO createAppNamespace(Env env, AppNamespaceDTO appNamespace) {
      return restTemplate
          .post(env, "apps/{appId}/appnamespaces", appNamespace, AppNamespaceDTO.class, appNamespace.getAppId());
    }

    public void deleteNamespace(Env env, String appId, String clusterName, String namespaceName, String operator) {
      restTemplate.delete(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}?operator={operator}", appId, clusterName,
                          namespaceName, operator);
    }

  }

  @Service
  public static class ItemAPI extends API {

    public List<ItemDTO> findItems(String appId, Env env, String clusterName, String namespaceName) {
      ItemDTO[] itemDTOs =
          restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
                           ItemDTO[].class, appId, clusterName, namespaceName);
      return Arrays.asList(itemDTOs);
    }

    public ItemDTO loadItem(Env env, String appId, String clusterName, String namespaceName, String key) {
      return restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{key}",
                              ItemDTO.class, appId, clusterName, namespaceName, key);
    }

    public void updateItemsByChangeSet(String appId, Env env, String clusterName, String namespace,
                                       ItemChangeSets changeSets) {
      restTemplate.post(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/itemset",
                        changeSets, Void.class, appId, clusterName, namespace);
    }

    public void updateItem(String appId, Env env, String clusterName, String namespace, long itemId, ItemDTO item) {
      restTemplate.put(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}",
                       item, appId, clusterName, namespace, itemId);

    }

    public ItemDTO createItem(String appId, Env env, String clusterName, String namespace, ItemDTO item) {
      return restTemplate.post(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/items",
                               item, ItemDTO.class, appId, clusterName, namespace);
    }

    public void deleteItem(Env env, long itemId, String operator) {

      restTemplate.delete(env, "items/{itemId}?operator={operator}", itemId, operator);
    }
  }

  @Service
  public static class ClusterAPI extends API {

    public List<ClusterDTO> findClustersByApp(String appId, Env env) {
      ClusterDTO[] clusterDTOs = restTemplate.get(env, "apps/{appId}/clusters", ClusterDTO[].class,
                                                  appId);
      return Arrays.asList(clusterDTOs);
    }

    public ClusterDTO loadCluster(String appId, Env env, String clusterName) {
      return restTemplate.get(env, "apps/{appId}/clusters/{clusterName}", ClusterDTO.class,
                              appId, clusterName);
    }

    public boolean isClusterUnique(String appId, Env env, String clusterName) {
      return restTemplate
          .get(env, "apps/{appId}/cluster/{clusterName}/unique", Boolean.class,
               appId, clusterName);

    }

    public ClusterDTO create(Env env, ClusterDTO cluster) {
      return restTemplate.post(env, "apps/{appId}/clusters", cluster, ClusterDTO.class,
                               cluster.getAppId());
    }

    public void delete(Env env, String appId, String clusterName, String operator) {
      restTemplate.delete(env, "apps/{appId}/clusters/{clusterName}?operator={operator}", appId, clusterName, operator);
    }
  }

  @Service
  public static class ReleaseAPI extends API {

    public ReleaseDTO loadRelease(Env env, long releaseId) {
      return restTemplate.get(env, "releases/{releaseId}", ReleaseDTO.class, releaseId);
    }

    public List<ReleaseDTO> findAllReleases(String appId, Env env, String clusterName, String namespaceName, int page,
                                            int size) {
      ReleaseDTO[] releaseDTOs = restTemplate.get(
          env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/all?page={page}&size={size}",
          ReleaseDTO[].class,
          appId, clusterName, namespaceName, page, size);
      return Arrays.asList(releaseDTOs);
    }

    public List<ReleaseDTO> findActiveReleases(String appId, Env env, String clusterName, String namespaceName,
                                               int page,
                                               int size) {
      ReleaseDTO[] releaseDTOs = restTemplate.get(
          env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active?page={page}&size={size}",
          ReleaseDTO[].class,
          appId, clusterName, namespaceName, page, size);
      return Arrays.asList(releaseDTOs);
    }

    public ReleaseDTO loadLatestRelease(String appId, Env env, String clusterName,
                                        String namespace) {
      ReleaseDTO releaseDTO = restTemplate
          .get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest",
               ReleaseDTO.class, appId, clusterName, namespace);
      return releaseDTO;
    }

    public ReleaseDTO createRelease(String appId, Env env, String clusterName, String namespace,
                                    String releaseTitle, String comment, String operator) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"));
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("name", releaseTitle);
      parameters.add("comment", comment);
      parameters.add("operator", operator);
      HttpEntity<MultiValueMap<String, String>> entity =
          new HttpEntity<>(parameters, headers);
      ReleaseDTO response = restTemplate.post(
          env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases", entity,
          ReleaseDTO.class,
          appId, clusterName, namespace);
      return response;
    }

    public void rollback(Env env, long releaseId, String operator) {
      restTemplate.put(env,
                       "releases/{releaseId}/rollback?operator={operator}",
                       null, releaseId, operator);
    }
  }

  @Service
  public static class CommitAPI extends API {

    public List<CommitDTO> find(String appId, Env env, String clusterName, String namespaceName, int page, int size) {

      CommitDTO[] commitDTOs = restTemplate.get(env,
                                                "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/commit?page={page}&size={size}",
                                                CommitDTO[].class,
                                                appId, clusterName, namespaceName, page, size);

      return Arrays.asList(commitDTOs);
    }
  }

  @Service
  public static class NamespaceLockAPI extends API {

    public NamespaceLockDTO getNamespaceLockOwner(String appId, Env env, String clusterName, String namespaceName) {
      return restTemplate.get(env, "apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/lock",
                              NamespaceLockDTO.class,
                              appId, clusterName, namespaceName);

    }
  }

  @Service
  public static class InstanceAPI extends API {
    private Joiner joiner = Joiner.on(",");
    private ParameterizedTypeReference<PageDTO<InstanceDTO>> pageInstanceDtoType = new ParameterizedTypeReference<PageDTO<InstanceDTO>>() {};

    public PageDTO<InstanceDTO> getByRelease(Env env, long releaseId, int page, int size){
      ResponseEntity<PageDTO<InstanceDTO>> entity = restTemplate.get(env, "/instances/by-release?releaseId={releaseId}&page={page}&size={size}", pageInstanceDtoType, releaseId, page, size);
      return entity.getBody();

    }

    public List<InstanceDTO> getByReleasesNotIn(String appId, Env env, String clusterName, String namespaceName, Set<Long> releaseIds){

      InstanceDTO[] instanceDTOs = restTemplate.get(env, "/instances/by-namespace-and-releases-not-in?appId={appId}&clusterName={clusterName}&namespaceName={namespaceName}&releaseIds={releaseIds}",
                                                    InstanceDTO[].class, appId, clusterName, namespaceName, joiner.join(releaseIds));

      return Arrays.asList(instanceDTOs);
    }

    public PageDTO<InstanceDTO> getByNamespace(String appId, Env env, String clusterName, String namespaceName, int page, int size){
      ResponseEntity<PageDTO<InstanceDTO>> entity = restTemplate.get(env, "/instances/by-namespace?appId={appId}&clusterName={clusterName}&namespaceName={namespaceName}&page={page}&size={size}",
                                                                     pageInstanceDtoType, appId, clusterName, namespaceName, page, size);
      return entity.getBody();
    }

    public int getInstanceCountByNamespace(String appId, Env env, String clusterName, String namespaceName){
      Integer count = restTemplate.get(env, "/instances/by-namespace/count?appId={appId}&clusterName={clusterName}&namespaceName={namespaceName}",
                                       Integer.class, appId, clusterName, namespaceName);
      if (count == null){
        return 0;
      }
      return count;
    }
  }

}