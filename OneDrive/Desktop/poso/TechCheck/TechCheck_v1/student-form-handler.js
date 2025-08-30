// JavaScript for handling the student form submission
// Add this script to your student.html page

// Configuration - Replace with your actual URLs
const GOOGLE_SCRIPT_URL = CONFIG?.GOOGLE_SCRIPT_URL || 'YOUR_GOOGLE_APPS_SCRIPT_WEB_APP_URL_HERE';
const HUBSPOT_FORM_URL = CONFIG?.HUBSPOT_FORM_URL || 'YOUR_HUBSPOT_FORM_ENDPOINT_HERE'; // Optional

// Form handler function
async function handleFormSubmission(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    // Convert FormData to regular object
    const data = {
        name: formData.get('name'),
        email: formData.get('email'),
        university: formData.get('university'),
        degreeType: formData.get('degreeType'),
        privacyConsent: formData.get('privacyConsent') === 'on',
        updatesConsent: formData.get('updatesConsent') === 'on'
    };
    
    try {
        // Show loading state
        const submitButton = form.querySelector('button[type="submit"]');
        const originalText = submitButton.textContent;
        submitButton.textContent = 'Submitting...';
        submitButton.disabled = true;
        
        // Submit to Google Sheets
        const response = await fetch(GOOGLE_SCRIPT_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        if (result.success) {
            // Success - show thank you message
            showSuccessMessage();
            form.reset();
        } else {
            throw new Error(result.message || 'Submission failed');
        }
        
        // Optional: Also submit to HubSpot
        if (HUBSPOT_FORM_URL && HUBSPOT_FORM_URL !== 'YOUR_HUBSPOT_FORM_ENDPOINT_HERE') {
            await submitToHubSpot(data);
        }
        
    } catch (error) {
        console.error('Form submission error:', error);
        showErrorMessage(error.message);
    } finally {
        // Restore button state
        const submitButton = form.querySelector('button[type="submit"]');
        submitButton.textContent = originalText;
        submitButton.disabled = false;
    }
}

// Optional HubSpot submission
async function submitToHubSpot(data) {
    try {
        const hubspotData = {
            fields: [
                { name: 'firstname', value: data.name.split(' ')[0] },
                { name: 'lastname', value: data.name.split(' ').slice(1).join(' ') },
                { name: 'email', value: data.email },
                { name: 'company', value: data.university },
                { name: 'degree_type', value: data.degreeType }
            ]
        };
        
        await fetch(HUBSPOT_FORM_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(hubspotData)
        });
    } catch (error) {
        console.error('HubSpot submission failed:', error);
        // Don't throw - we don't want to show error to user if main submission succeeded
    }
}

// Show success message
function showSuccessMessage() {
    const successHTML = `
        <div class="bg-green-50 border border-green-200 rounded-md p-4 mb-4">
            <div class="flex">
                <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                    </svg>
                </div>
                <div class="ml-3">
                    <h3 class="text-sm font-medium text-green-800">Success!</h3>
                    <p class="mt-1 text-sm text-green-700">Thank you for joining our early access list. We'll be in touch soon!</p>
                </div>
            </div>
        </div>
    `;
    
    const form = document.getElementById('early-access-form');
    form.innerHTML = successHTML;
}

// Show error message
function showErrorMessage(message) {
    const errorHTML = `
        <div class="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
            <div class="flex">
                <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                    </svg>
                </div>
                <div class="ml-3">
                    <h3 class="text-sm font-medium text-red-800">Error!</h3>
                    <p class="mt-1 text-sm text-red-700">There was an error submitting your form: ${message}</p>
                </div>
            </div>
        </div>
    `;
    
    const formContainer = document.querySelector('#early-access-form .p-6');
    formContainer.insertAdjacentHTML('afterbegin', errorHTML);
}

// Initialize form when page loads
document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('#early-access-form form');
    if (form) {
        form.addEventListener('submit', handleFormSubmission);
    }
});