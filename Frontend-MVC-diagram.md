`graph TD
      subgraph "User Interface (View)"
          direction LR
          view_xhtml["transacciones.xhtml / login.xhtml"]
      end

      subgraph "Controller Logic (Controller)"
          direction LR
          controller_bean["TransactionBean / LoginBean"]
      end

      subgraph "Data & Services (Model)"
          direction LR
          model_client["TransactionRestClient"]
          model_dto["DTOs (ApiResponse, etc.)"]
      end

      %% Interactions
      User -- "Interacts with" --> view_xhtml
      view_xhtml -- "Triggers Actions" --> controller_bean
      controller_bean -- "Updates" --> view_xhtml
      controller_bean -- "Calls Service" --> model_client
      model_client -- "Uses Data Structures" --> model_dto
      model_client -- "Communicates with" --> BackendAPI[Backend API]

`