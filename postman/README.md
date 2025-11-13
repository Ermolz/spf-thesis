# Postman Collections for Freelance Marketplace API

This directory contains Postman collections for testing the Freelance Marketplace API.

## Collections

### Core Collections
1. **Authentication.postman_collection.json** - User registration and login
2. **Projects.postman_collection.json** - Project management (create, update, search, publish)
3. **Proposals.postman_collection.json** - Proposal management (create, accept, reject, withdraw)
4. **Assignments.postman_collection.json** - Assignment/contract management
5. **Tasks.postman_collection.json** - Task management and file attachments
6. **Chat.postman_collection.json** - Messaging and conversations
7. **Payments.postman_collection.json** - Payment management (escrow, releases)
8. **Payouts.postman_collection.json** - Payout requests for freelancers
9. **Reviews.postman_collection.json** - Review and rating management
10. **UserProfiles.postman_collection.json** - User profile management

### Flow Collections
- **Flows/Client_Flow.postman_collection.json** - Complete client workflow
- **Flows/Freelancer_Flow.postman_collection.json** - Complete freelancer workflow

## Setup Instructions

1. **Import Collections**: Import all JSON files into Postman
2. **Create Environment**: Create a new environment with these variables:
   - `base_url`: `http://localhost:8080`
   - `auth_token`: (will be set automatically after login)
   - `user_id`: (will be set automatically)
   - `user_role`: (will be set automatically)
   - `user_email`: (will be set automatically)

3. **Run Flows**: Use the Flow collections to test complete workflows

## Features

- ✅ All requests include authentication (Bearer token)
- ✅ Test scripts validate response structure (success, data, metadata, errors)
- ✅ Automatic token saving after login/register
- ✅ Automatic ID saving for chained requests
- ✅ Comprehensive error handling tests
- ✅ Pagination support in list endpoints

## Response Format

All API responses follow this structure:

**Success Response:**
```json
{
  "success": true,
  "data": {...},
  "metadata": {
    "timestamp": "2024-01-15T10:30:00Z",
    "pagination": {...}
  }
}
```

**Error Response:**
```json
{
  "success": false,
  "errors": [
    {
      "code": "ERROR_CODE",
      "message": "Error message",
      "field": "fieldName",
      "status": 400,
      "path": "/api/endpoint",
      "timestamp": "2024-01-15T10:30:00Z"
    }
  ]
}
```

## Test Scripts

Each request includes Postman test scripts that verify:
- HTTP status code
- Response structure (success, data, metadata)
- Data validation
- Automatic variable saving for chained requests

## Usage Tips

1. Start with **Authentication** collection to get a token
2. Use **Flow** collections for end-to-end testing
3. Individual collections can be used for specific feature testing
4. All collections use environment variables for easy configuration

