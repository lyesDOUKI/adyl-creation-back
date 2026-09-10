# Changelog

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
