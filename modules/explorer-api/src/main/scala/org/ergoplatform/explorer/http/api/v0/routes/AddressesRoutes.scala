package org.ergoplatform.explorer.http.api.v0.routes

import cats.effect.{ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.semigroupk._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import org.ergoplatform.explorer.http.api.ApiErr
import org.ergoplatform.explorer.http.api.algebra.AdaptThrowable.AdaptThrowableEitherT
import org.ergoplatform.explorer.http.api.models.Items
import org.ergoplatform.explorer.http.api.syntax.adaptThrowable._
import org.ergoplatform.explorer.http.api.v0.services.{AddressesService, TransactionsService}
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s._

final class AddressesRoutes[
  F[_]: Sync: ContextShift: AdaptThrowableEitherT[*[_], ApiErr]
](
  addressesService: AddressesService[F, Stream],
  transactionsService: TransactionsService[F]
)(implicit opts: Http4sServerOptions[F]) {

  import org.ergoplatform.explorer.http.api.v0.defs.AddressesEndpointDefs._

  val routes: HttpRoutes[F] =
    getAddressR <+> getTxsByAddressR <+> getAssetHoldersR

  def getAddressR: HttpRoutes[F] =
    getAddressDef.toRoutes { case (address, minConfirmations) =>
      addressesService.getAddressInfo(address, minConfirmations).adaptThrowable.value
    }

  def getTxsByAddressR: HttpRoutes[F] =
    getTxsByAddressDef.toRoutes {
      case (address, paging) =>
        transactionsService
          .getTxsInfoByAddress(address, paging)
          .adaptThrowable
          .value
    }

  def getAssetHoldersR: HttpRoutes[F] =
    getAssetHoldersDef.toRoutes {
      case (tokenId, paging) =>
        addressesService
          .getAssetHoldersAddresses(tokenId, paging)
          .compile
          .toList
          .adaptThrowable
          .value
    }
}

object AddressesRoutes {

  def apply[F[_]: Sync: ContextShift: Logger](
    addressesService: AddressesService[F, Stream],
    transactionsService: TransactionsService[F]
  )(implicit opts: Http4sServerOptions[F]): HttpRoutes[F] =
    new AddressesRoutes(addressesService, transactionsService).routes
}