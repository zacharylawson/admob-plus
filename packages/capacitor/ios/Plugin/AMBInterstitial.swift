import Capacitor
import GoogleMobileAds

class AMBInterstitial: AMBAdBase, GADFullScreenContentDelegate {
    var interstitial: GADInterstitialAd?

    deinit {
        interstitial?.fullScreenContentDelegate = nil
    }

    func isLoaded() -> Bool {
        return self.interstitial != nil
    }

    func load(_ ctx: AMBContext) {
        GADInterstitialAd.load(
            withAdUnitID: adUnitId,
            request: ctx.optGADRequest(),
            completionHandler: { ad, error in
                if error != nil {
                    self.emit(AMBEvents.rewardedInterstitialLoadFail, error!)
                    ctx.error(error!)
                    return
                }

                self.interstitial = ad
                ad?.fullScreenContentDelegate = self

                self.emit(AMBEvents.interstitialLoad)
                ctx.success()
         })
    }

    func show(_ ctx: AMBContext) {
        if self.isLoaded() {
            self.interstitial?.present(fromRootViewController: AMBContext.rootViewController)
            ctx.success()
        } else {
            ctx.error("Ad is not loaded")
        }
    }

    func adDidRecordImpression(_ ad: GADFullScreenPresentingAd) {
        self.emit(AMBEvents.interstitialImpression)
    }

    func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        self.emit(AMBEvents.interstitialShowFail, error)
    }

    func adDidPresentFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        self.emit(AMBEvents.interstitialShow)
    }

    func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        self.emit(AMBEvents.interstitialDismiss)
    }
}
