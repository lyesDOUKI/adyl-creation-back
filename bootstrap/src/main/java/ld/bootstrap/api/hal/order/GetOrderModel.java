package ld.bootstrap.api.hal.order;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.bootstrap.response.GetOrderResponse;
import org.springframework.hateoas.EntityModel;

@Schema(name = "GetOrderHalResponse", description = "Commande au format HAL")
public class GetOrderModel extends EntityModel<GetOrderResponse> {}