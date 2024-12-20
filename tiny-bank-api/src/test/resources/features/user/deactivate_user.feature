Feature: Deactivate User

  The Tiny User Bank can at any moment choose to close its account in the Tiny Bank. The user is not deleted from the bank
  data records. It is deactivated to allow future data verification.

  **As a** client that has an account at Tiny Bank
  **I want to** request the account deactivation
  **So that I can** end my relationship with the bank

  Rule: When a Tiny Bank client request to deactivate its account, based on the clients national ID the deactivation is done.

    @pending
    Scenario: Deactivate an account for a national ID
      Given the client identification for document type "National ID", document "abcdefg", country "PT"
      And there is a client with the document type "National ID", document "abcdefg", country "PT"
      When the account deactivation is requested
      Then the client's account is deactivated

    @pending
    Scenario: Deactivate an account for a non existing national ID
      Given the client identification for document type "National ID", document "12343532", country "PT"
      When the account deactivation is requested
      And the client identification have no record in Tiny Bank
      Then the client's account deactivation is denied