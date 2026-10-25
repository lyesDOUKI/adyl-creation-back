# Changelog

## [5.1.0](https://github.com/lyesDOUKI/adyl-creation-back/compare/v5.0.0...v5.1.0) (2026-10-25)


### Features

* uniformize naming for slots times + simplify naming for component in the openapi spec ([763a07d](https://github.com/lyesDOUKI/adyl-creation-back/commit/763a07d8285623350c6a3b4385b14339dee81c48))

<!-- api-contract:start -->
### API Contract

API `1.0.0` → `2.0.0` : ⚠️ **breaking change**.

- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /appointments — added `#/components/schemas/SecurityForbidden` to the response body `oneOf` list for the response status `403`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /appointments — added `#/components/schemas/SecurityUnauthenticated` to the response body `oneOf` list for the response status `401`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /appointments — added `#/components/schemas/ServerInternalError` to the response body `oneOf` list for the response status `500`
- ❌ **ERR** — [new-required-request-property] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — added the new required request property `endAt`
- ❌ **ERR** — [new-required-request-property] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — added the new required request property `startAt`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — added `#/components/schemas/RequestValidationFailed, #/components/schemas/RequestInvalidRequestBody` to the response body `oneOf` list for the response status `400`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — added `#/components/schemas/SecurityForbidden` to the response body `oneOf` list for the response status `403`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — added `#/components/schemas/SecurityUnauthenticated` to the response body `oneOf` list for the response status `401`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — added `#/components/schemas/ServerInternalError` to the response body `oneOf` list for the response status `500`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — added `#/components/schemas/SubmitAppointmentSlotAlreadyBooked` to the response body `oneOf` list for the response status `409`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — added `#/components/schemas/SubmitAppointmentSlotOutsideOpeningHours` to the response body `oneOf` list for the response status `422`
- ❌ **ERR** — [response-required-property-removed] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — removed the required property `end` from the response with the `201` status
- ❌ **ERR** — [response-required-property-removed] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /appointments — removed the required property `start` from the response with the `201` status
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /appointments/available-slots — added `#/components/schemas/RequestInvalidParameter` to the response body `oneOf` list for the response status `400`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /appointments/available-slots — added `#/components/schemas/SecurityForbidden` to the response body `oneOf` list for the response status `403`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /appointments/available-slots — added `#/components/schemas/SecurityUnauthenticated` to the response body `oneOf` list for the response status `401`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /appointments/available-slots — added `#/components/schemas/ServerInternalError` to the response body `oneOf` list for the response status `500`
- ❌ **ERR** — [response-required-property-removed] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /appointments/available-slots — removed the required property `availability/items/slots/items/end` from the response with the `200` status
- ❌ **ERR** — [response-required-property-removed] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /appointments/available-slots — removed the required property `availability/items/slots/items/start` from the response with the `200` status
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /customer/me — added `#/components/schemas/RequestValidationFailed, #/components/schemas/RequestInvalidRequestBody` to the response body `oneOf` list for the response status `400`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /customer/me — added `#/components/schemas/SecurityForbidden` to the response body `oneOf` list for the response status `403`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /customer/me — added `#/components/schemas/SecurityUnauthenticated` to the response body `oneOf` list for the response status `401`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /customer/me — added `#/components/schemas/ServerInternalError` to the response body `oneOf` list for the response status `500`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /orders — added `#/components/schemas/RequestInvalidSortField` to the response body `oneOf` list for the response status `400`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /orders — added `#/components/schemas/SecurityForbidden` to the response body `oneOf` list for the response status `403`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /orders — added `#/components/schemas/SecurityUnauthenticated` to the response body `oneOf` list for the response status `401`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /orders — added `#/components/schemas/ServerInternalError` to the response body `oneOf` list for the response status `500`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders — added `#/components/schemas/CreateOrderProductNotAvailable` to the response body `oneOf` list for the response status `409`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders — added `#/components/schemas/CreateOrderProductsNotFound, #/components/schemas/CreateOrderProductColorNotAvailable` to the response body `oneOf` list for the response status `422`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders — added `#/components/schemas/RequestValidationFailed, #/components/schemas/RequestInvalidRequestBody` to the response body `oneOf` list for the response status `400`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders — added `#/components/schemas/SecurityForbidden` to the response body `oneOf` list for the response status `403`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders — added `#/components/schemas/SecurityUnauthenticated` to the response body `oneOf` list for the response status `401`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders — added `#/components/schemas/ServerInternalError` to the response body `oneOf` list for the response status `500`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /orders/{id} — added `#/components/schemas/GetOrderOrderNotFound` to the response body `oneOf` list for the response status `404`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /orders/{id} — added `#/components/schemas/RequestInvalidParameter` to the response body `oneOf` list for the response status `400`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /orders/{id} — added `#/components/schemas/SecurityForbidden` to the response body `oneOf` list for the response status `403`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /orders/{id} — added `#/components/schemas/SecurityUnauthenticated` to the response body `oneOf` list for the response status `401`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API GET /orders/{id} — added `#/components/schemas/ServerInternalError` to the response body `oneOf` list for the response status `500`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/accept — added `#/components/schemas/AcceptOrderOrderAlreadyRejected, #/components/schemas/AcceptOrderOrderAlreadyDelivered` to the response body `oneOf` list for the response status `409`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/accept — added `#/components/schemas/AcceptOrderOrderNotFound` to the response body `oneOf` list for the response status `404`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/accept — added `#/components/schemas/RequestInvalidParameter` to the response body `oneOf` list for the response status `400`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/accept — added `#/components/schemas/SecurityForbidden` to the response body `oneOf` list for the response status `403`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/accept — added `#/components/schemas/SecurityUnauthenticated` to the response body `oneOf` list for the response status `401`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/accept — added `#/components/schemas/ServerInternalError` to the response body `oneOf` list for the response status `500`
- ❌ **ERR** — [response-required-property-removed] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/accept — removed the required property `orderState` from the response with the `200` status
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/deliver — added `#/components/schemas/DeliverOrderOrderAlreadyRejected` to the response body `oneOf` list for the response status `409`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/deliver — added `#/components/schemas/DeliverOrderOrderNotFound` to the response body `oneOf` list for the response status `404`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/deliver — added `#/components/schemas/DeliverOrderOrderStillPending` to the response body `oneOf` list for the response status `422`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/deliver — added `#/components/schemas/RequestValidationFailed, #/components/schemas/RequestInvalidRequestBody, #/components/schemas/RequestInvalidParameter` to the response body `oneOf` list for the response status `400`
- ❌ **ERR** — [response-body-one-of-added] at /home/runner/work/_temp/head-openapi-normalized.yaml — in API POST /orders/{id}/deliver — added `#/components/schemas/SecurityForbidden` to the response body `oneOf` list for the response status `403`

… et 222 autre(s) changement(s) : voir le commentaire de la PR de release.
<!-- api-contract:end -->

## [5.0.0](https://github.com/lyesDOUKI/adyl-creation-back/compare/v4.0.0...v5.0.0) (2026-10-25)


### ⚠ BREAKING CHANGES

* new design of the api documentation - API management as code

### Features

* add more documentation for api problems ([1efab4f](https://github.com/lyesDOUKI/adyl-creation-back/commit/1efab4fd53c83b85423e89896fac6db3c5c34168))
* add versionning tools for openapi doc ([1a2f757](https://github.com/lyesDOUKI/adyl-creation-back/commit/1a2f757c28d2e3d7d955e894cef10fa8a220db85))
* api versioning mecanism ([1b13786](https://github.com/lyesDOUKI/adyl-creation-back/commit/1b13786f0a7720e90af6f817ebd47b2cafd428fc))
* dynamic resolve for instance in http response problem details examples ([2c47587](https://github.com/lyesDOUKI/adyl-creation-back/commit/2c47587bf5b89fbeed04024bb85941a661dc5c0e))
* handle dates as instant - improve apidoc ([bd13306](https://github.com/lyesDOUKI/adyl-creation-back/commit/bd13306e2408208d75158831446728c701f1c3f6))
* introduce api versionning mecanism ([8c028ce](https://github.com/lyesDOUKI/adyl-creation-back/commit/8c028ce95f4b9c7fd0561f0323f4b1d0e609c7b6))
* modify Result error pattern ([59b134c](https://github.com/lyesDOUKI/adyl-creation-back/commit/59b134c2847514dad1766b5fa24cdb743edbf03d))
* new design of the api documentation - API management as code ([888c2ec](https://github.com/lyesDOUKI/adyl-creation-back/commit/888c2ec5956a0e46d3443f46374408274729d1e0))

<!-- api-contract:start -->
### API Contract

🆕 Première version de l'API publiée : `1.0.0`.
<!-- api-contract:end -->

## [4.0.0](https://github.com/lyesDOUKI/adyl-creation-back/compare/v3.1.0...v4.0.0) (2026-10-13)


### ⚠ BREAKING CHANGES

* improve Pattern Result by explicitely add the Error sides - modelize Error as domain models

### Features

* improve Pattern Result by explicitely add the Error sides - modelize Error as domain models ([4f0f324](https://github.com/lyesDOUKI/adyl-creation-back/commit/4f0f32410a470bd1b9d357a5e5e224a8e7bfdc71))

## [3.1.0](https://github.com/lyesDOUKI/adyl-creation-back/compare/v3.0.0...v3.1.0) (2026-09-14)


### Features

* handle race condition when submitting Appointment ([e9e5f77](https://github.com/lyesDOUKI/adyl-creation-back/commit/e9e5f77ffdddc2be1ca9fc29d5dbd16332155a4e))

## [3.0.0](https://github.com/lyesDOUKI/adyl-creation-back/compare/v2.2.1...v3.0.0) (2026-09-12)


### ⚠ BREAKING CHANGES

* add creating appointment endpoint
* add available slots finder in the application layer - representation by date
* add submiting appointment usecase
* add available slots finder usecase

### Features

* add available slots finder in the application layer - representation by date ([b1da8b1](https://github.com/lyesDOUKI/adyl-creation-back/commit/b1da8b144f1bf06cb245de2dc5b754e5568ec76e))
* add available slots finder usecase ([b454d8a](https://github.com/lyesDOUKI/adyl-creation-back/commit/b454d8ad7253b115b1723afc2ae3cee7c156379c))
* add creating appointment endpoint ([ec27cb1](https://github.com/lyesDOUKI/adyl-creation-back/commit/ec27cb15f56fbe89f26ae94494d04d3690b9c8c3))
* add submiting appointment usecase ([0253e7c](https://github.com/lyesDOUKI/adyl-creation-back/commit/0253e7c8cceb82039e82f17227021acc3dd3f26c))
* stock notes Appointment ([704ae89](https://github.com/lyesDOUKI/adyl-creation-back/commit/704ae89ad54c6e8ead0a77a450337babc73cebf6))


### Bug Fixes

* make spring dispatcher as primary bean for orders ([595635d](https://github.com/lyesDOUKI/adyl-creation-back/commit/595635d86d0147ce3151658a1acceccf33e2f3b7))

## [2.2.1](https://github.com/lyesDOUKI/adyl-creation-back/compare/v2.2.0...v2.2.1) (2026-09-10)


### Bug Fixes

* don't give customer name in the object of mail ([375fb49](https://github.com/lyesDOUKI/adyl-creation-back/commit/375fb49493ad428675a13f0e1356d5224157a37b))

## [2.2.0](https://github.com/lyesDOUKI/adyl-creation-back/compare/v2.1.0...v2.2.0) (2026-09-10)


### Features

* introduce unique order reference, as value object ([2f529b0](https://github.com/lyesDOUKI/adyl-creation-back/commit/2f529b0278af29b8b52723b72240c9cf1d87c25a))


### Documentation

* add .idea in gitignore ([5810be5](https://github.com/lyesDOUKI/adyl-creation-back/commit/5810be59726c2dee07975a538c4a6cb7f3ab0caf))

## [2.1.0](https://github.com/lyesDOUKI/adyl-creation-back/compare/v2.0.0...v2.1.0) (2026-09-10)


### Features

* clean order request, no need for personnal information ([964c32b](https://github.com/lyesDOUKI/adyl-creation-back/commit/964c32b2556bd1f65f03e29966e1d8766344d021))


### Bug Fixes

* order created template dont print client name ([d5a64ac](https://github.com/lyesDOUKI/adyl-creation-back/commit/d5a64ac35ad9a38237ad3b7fc49e73e5fedf559f))

## [2.0.0](https://github.com/lyesDOUKI/adyl-creation-back/compare/v1.2.0...v2.0.0) (2026-09-10)


### ⚠ BREAKING CHANGES

* add endpoint to register customer
* introduce customer register service
* add endpoint for get order + introduce customerId extractor

### Features

* add endpoint for get order + introduce customerId extractor ([f365ee6](https://github.com/lyesDOUKI/adyl-creation-back/commit/f365ee6a67721c54720d00f1e2b227d46f27383a))
* add endpoint to register customer ([287c38a](https://github.com/lyesDOUKI/adyl-creation-back/commit/287c38ab8c709cd662e07ea572f8bfa3b3e84fba))
* add get order service + integration tests ([f7e6fa1](https://github.com/lyesDOUKI/adyl-creation-back/commit/f7e6fa19b830d528f2e3d8e6745b72d9a550a1a0))
* add mecanism to extract customerId ([ba9f5a5](https://github.com/lyesDOUKI/adyl-creation-back/commit/ba9f5a5e384517e5e77a219bf236a0ea9e9c9172))
* introduce a new data model (DB) for order life cycle ([9bbb34e](https://github.com/lyesDOUKI/adyl-creation-back/commit/9bbb34e6f13a7ff742079d7d0f501ea9c6f32ab9))
* introduce conventional standard ([334042c](https://github.com/lyesDOUKI/adyl-creation-back/commit/334042c30ae3e4e9a384d0bd3aecf80f061a45c1))
* introduce customer register service ([097f10d](https://github.com/lyesDOUKI/adyl-creation-back/commit/097f10d7fb6a3c1b2db7dfc78f0f5c7a45151bb2))
* introduce new persistence for order: address delivery ([a1d42d6](https://github.com/lyesDOUKI/adyl-creation-back/commit/a1d42d646f30fa265b898d1839d897a4c88d1587))


### Bug Fixes

* do fix on tests using previous model of customer table ([3170a50](https://github.com/lyesDOUKI/adyl-creation-back/commit/3170a50bcc02be8a42c837e017d3b60567293723))


### Documentation

* make don't add component in the tag when release please ([6ffc8a8](https://github.com/lyesDOUKI/adyl-creation-back/commit/6ffc8a8644b4c7ae16caf69fd1ee95b237ea3781))
* make latest release in .release-please-manifest.json ([9bb2dc7](https://github.com/lyesDOUKI/adyl-creation-back/commit/9bb2dc7794694e98b1d02dfe3b2ff2ade6207a32))
