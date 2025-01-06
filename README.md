# Lavender

Only the API is exposed. Wait for the API design to be complete. Core modules will be added.

## TODO

  - [ ] EventBus (EventManager)
  - [ ] Accessor bytecode generator
    - [x] InstanceCheck (Generate `instanceof` check for class in jvm)
    - [ ] Interface Accessor (Like mixin `@Accessor` but proxied getter/setter for instance. Compile at runtime)
      - [ ] `@NotNull` & `@Nullable` Check.
      - [ ] Auto cast type as another accessor
      - [ ] Fallback method on failed
      - [x] Search target based by type index in target fields / methods.
      - [ ] Modify `final` fields by using `Unsafe`
  - [ ] Plugin Manager