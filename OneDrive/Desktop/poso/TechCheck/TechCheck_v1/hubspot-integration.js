// HubSpot Forms API Integration
// Alternative approach using HubSpot's Forms API directly

// Configuration
const HUBSPOT_PORTAL_ID = 'YOUR_HUBSPOT_PORTAL_ID';
const HUBSPOT_FORM_GUID = 'YOUR_HUBSPOT_FORM_GUID';

async function submitToHubSpotFormsAPI(formData) {
    const hubspotURL = `https://api.hsforms.com/submissions/v3/integration/submit/${HUBSPOT_PORTAL_ID}/${HUBSPOT_FORM_GUID}`;
    
    // Map form fields to HubSpot properties
    const hubspotData = {
        fields: [
            {
                objectTypeId: "0-1", // Contact
                name: "firstname",
                value: formData.name.split(' ')[0] || ''
            },
            {
                objectTypeId: "0-1",
                name: "lastname", 
                value: formData.name.split(' ').slice(1).join(' ') || ''
            },
            {
                objectTypeId: "0-1",
                name: "email",
                value: formData.email
            },
            {
                objectTypeId: "0-1",
                name: "company",
                value: formData.university
            },
            {
                objectTypeId: "0-1",
                name: "degree_type", // Custom property you need to create in HubSpot
                value: formData.degreeType
            }
        ],
        context: {
            pageUri: window.location.href,
            pageName: document.title
        }
    };
    
    try {
        const response = await fetch(hubspotURL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(hubspotData)
        });
        
        if (!response.ok) {
            throw new Error(`HubSpot API error: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('HubSpot submission failed:', error);
        throw error;
    }
}

// Alternative: Use HubSpot's embedded form (easier setup)
function embedHubSpotForm() {
    const script = document.createElement('script');
    script.src = '//js.hs-scripts.com/YOUR_PORTAL_ID.js';
    script.onload = function() {
        hbspt.forms.create({
            region: "na1", // or "eu1" for European accounts
            portalId: "YOUR_PORTAL_ID",
            formId: "YOUR_FORM_ID",
            target: "#hubspot-form-container"
        });
    };
    document.head.appendChild(script);
}