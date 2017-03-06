AUI.add(
	'liferay-adaptivemedia',
	function(A) {
		var STR_CHANGE = 'change';

		var AdaptiveMedia = A.Component.create(
			{
				ATTRS: {
				},

				AUGMENTS: [Liferay.PortletBase],

				EXTENDS: A.Base,

				NAME: 'liferay-adaptivemedia',

				NS: 'liferay-adaptivemedia',

				prototype: {
					initializer: function(config) {
						var instance = this;

						instance._bindUI();
					},

					destructor: function() {
						var instance = this;

						if (instance._saveDraftTimer) {
							instance._saveDraftTimer.cancel();
						}

						(new A.EventHandle(instance._eventHandles)).detach();
					},

					updateUuid: function(title) {
						var instance = this;

						var uuidTitleInput = instance.one('#uuidTitle');

						var uuidEmpty = !uuidTitleInput.val();

						if (instance._automaticUuid() && (uuidEmpty || instance._originalUuidChanged)) {
							uuidTitleInput.val(Liferay.Util.normalizeFriendlyURL(title));
						}

						instance._originalUuidChanged = true;
					},

					_bindUI: function() {
						var instance = this;

						var idOptions = instance.one('#idOptions');

						var eventHandles = [
							idOptions.delegate(STR_CHANGE, instance._onChangeUuidOptions, 'input[type="radio"]', instance)
						];

						instance._eventHandles = eventHandles;
					},

					_onChangeUuidOptions: function() {
						var instance = this;

						var uuidTitleInput = instance.one('#uuidTitle');

						if (instance._automaticUuid()) {
							instance._lastCustomUuuid = uuidTitleInput.val();

							var title = instance.one('#name').val();

							instance.updateUuid(title);

							uuidTitleInput.setAttribute('disabled', true);
						}
						else {
							uuidTitleInput.val(instance._lastCustomUuuid || uuidTitleInput.val());

							uuidTitleInput.removeAttribute('disabled');
						}
					},

					_automaticUuid: function() {
						return this.one('#idOptions').one("input:checked").val() === 'true';
					}
				}
			}
		);

		Liferay.AdaptiveMedia = AdaptiveMedia;
	},
	'',
	{
		requires: ['aui-base', 'aui-io-request', 'liferay-portlet-base']
	}
);