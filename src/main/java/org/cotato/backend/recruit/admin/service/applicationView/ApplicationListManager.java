// package org.cotato.backend.recruit.admin.service.applicationView;

// import
// org.cotato.backend.recruit.admin.dto.request.applicationView.ApplicationListRequest;
// import org.cotato.backend.recruit.domain.application.entity.Application;
// import org.cotato.backend.recruit.domain.application.enums.PassStatus;
// import org.cotato.backend.recruit.domain.generation.entity.Generation;
// import org.cotato.backend.recruit.domain.question.enums.PartType;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.data.domain.Sort.Order;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.stereotype.Component;

// @Component
// public class ApplicationListManager {

// public Specification<Application> makeSpecification(
// ApplicationListRequest request, Generation generation) {

// return Specification
// // 기수별 검색(필수)
// .where(ApplicationSpecification.equalGeneration(generation))
// // 학교 like %keyword% Or 이름 like %keyword% 검색
// // 기수별 지원서가 많지 않기 때문에 전문검색 필요X
// .and(ApplicationSpecification.likeSearchKeyword(request.getSearchKeyword()))
// // 파트별 검색(선택)
// .and(
// ApplicationSpecification.equalPartType(
// resolvePartType(request.getViewPartType())))
// // 합격여부 검색(선택)
// .and(
// ApplicationSpecification.equalPassStatus(
// resolvePassStatus(request.getPassStatus())));
// }

// public Pageable makePageable(Pageable pageable) {
// if (pageable.getSort().isUnsorted()) {
// return pageable;
// }

// Sort newSort = Sort.unsorted();
// for (Order order : pageable.getSort()) {
// Sort currentSort = getMappedSort(order);
// if (newSort.isUnsorted()) {
// newSort = currentSort;
// } else {
// newSort = newSort.and(currentSort);
// }
// }

// return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
// newSort);
// }

// private Sort getMappedSort(Order order) {
// String property = order.getProperty();
// boolean isAsc = order.isAscending();
// String mappedProperty = property;

// switch (property) {
// case "university":
// mappedProperty = "university";
// break;
// case "name":
// mappedProperty = "name";
// break;
// default:
// mappedProperty = property; // Fallback
// break;
// }

// return isAsc ? Sort.by(mappedProperty).ascending() :
// Sort.by(mappedProperty).descending();
// }

// private PartType resolvePartType(String partParam) {
// if (partParam == null || partParam.isBlank()) {
// return null;
// }
// return PartType.fromString(partParam);
// }

// private PassStatus resolvePassStatus(String statusParam) {
// if (statusParam == null || statusParam.isBlank()) {
// return null;
// }
// return PassStatus.fromString(statusParam);
// }
// }
