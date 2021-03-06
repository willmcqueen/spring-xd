/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.x.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.MediaType;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.xd.tuple.Tuple;
import org.springframework.xd.tuple.TupleBuilder;

/**
 * @author Gary Russell
 */
public class MessageBusSupportTests {

	private final TestMessageBus messageBus = new TestMessageBus();

	@Test
	public void testBytesPassThru() {
		byte[] payload = "foo".getBytes();
		Message<byte[]> message = MessageBuilder.withPayload(payload).build();
		Message<?> converted = messageBus.transformOutboundIfNecessary(message,
				MediaType.APPLICATION_OCTET_STREAM);
		assertSame(payload, converted.getPayload());
		assertEquals(MessageBusSupport.XD_OCTET_STREAM_VALUE,
				converted.getHeaders().get(MessageHeaders.CONTENT_TYPE));
		Message<?> reconstructed = messageBus.transformInboundIfNecessary(converted,
				Collections.singletonList(MediaType.ALL));
		payload = (byte[]) reconstructed.getPayload();
		assertSame(converted.getPayload(), payload);
		assertNull(reconstructed.getHeaders().get(MessageHeaders.CONTENT_TYPE));
	}

	@Test
	public void testBytesPassThruContentType() {
		byte[] payload = "foo".getBytes();
		Message<byte[]> message = MessageBuilder.withPayload(payload)
				.setHeader(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
				.build();
		Message<?> converted = messageBus.transformOutboundIfNecessary(message,
				MediaType.APPLICATION_OCTET_STREAM);
		assertSame(payload, converted.getPayload());
		assertEquals(MessageBusSupport.XD_OCTET_STREAM_VALUE,
				converted.getHeaders().get(MessageHeaders.CONTENT_TYPE));
		Message<?> reconstructed = messageBus.transformInboundIfNecessary(converted,
				Collections.singletonList(MediaType.ALL));
		payload = (byte[]) reconstructed.getPayload();
		assertSame(converted.getPayload(), payload);
		assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE,
				reconstructed.getHeaders().get(MessageHeaders.CONTENT_TYPE));
		assertNull(reconstructed.getHeaders().get(MessageBusSupport.ORIGINAL_CONTENT_TYPE_HEADER));
	}

	@Test
	public void testString() {
		Message<?> converted = messageBus.transformOutboundIfNecessary(
				new GenericMessage<String>("foo"), MediaType.APPLICATION_OCTET_STREAM);
		assertEquals("foo", new String((byte[]) converted.getPayload()));
		assertEquals(MessageBusSupport.XD_TEXT_PLAIN_UTF8_VALUE,
				converted.getHeaders().get(MessageHeaders.CONTENT_TYPE));
		Message<?> reconstructed = messageBus.transformInboundIfNecessary(converted,
				Collections.singletonList(MediaType.ALL));
		assertEquals("foo", reconstructed.getPayload());
		assertNull(reconstructed.getHeaders().get(MessageHeaders.CONTENT_TYPE));
	}

	@Test
	public void testJsonPojo() {
		Message<?> converted = messageBus.transformOutboundIfNecessary(new GenericMessage<Foo>(new Foo("bar")),
				MediaType.APPLICATION_OCTET_STREAM);
		assertEquals(
				"{\"Foo\":{\"@class\":\"org.springframework.integration.x.bus.MessageBusSupportTests$Foo\",\"bar\":\"bar\"}}",
				new String((byte[]) converted.getPayload()));
		assertEquals(MessageBusSupport.XD_JSON_OCTET_STREAM_VALUE,
				converted.getHeaders().get(MessageHeaders.CONTENT_TYPE));
		Message<?> reconstructed = messageBus.transformInboundIfNecessary(converted,
				Collections.singletonList(MediaType.ALL));
		assertEquals("bar", ((Foo) reconstructed.getPayload()).getBar());
		assertNull(reconstructed.getHeaders().get(MessageHeaders.CONTENT_TYPE));
	}

	@Test
	public void testJsonPojoWithXJavaObjectMediaTypeNoType() {
		Message<?> converted = messageBus.transformOutboundIfNecessary(new GenericMessage<Foo>(new Foo("bar")),
				MediaType.APPLICATION_OCTET_STREAM);
		assertEquals(
				"{\"Foo\":{\"@class\":\"org.springframework.integration.x.bus.MessageBusSupportTests$Foo\",\"bar\":\"bar\"}}",
				new String((byte[]) converted.getPayload()));
		assertEquals(MessageBusSupport.XD_JSON_OCTET_STREAM_VALUE,
				converted.getHeaders().get(MessageHeaders.CONTENT_TYPE));
		Message<?> reconstructed = messageBus.transformInboundIfNecessary(converted,
				Collections.singletonList(new MediaType("application", "x-java-object")));
		assertEquals("bar", ((Foo) reconstructed.getPayload()).getBar());
		assertNull(reconstructed.getHeaders().get(MessageHeaders.CONTENT_TYPE));
	}

	@Test
	public void testJsonPojoWithXJavaObjectMediaTypeExplicitType() {
		Message<?> converted = messageBus.transformOutboundIfNecessary(new GenericMessage<Foo>(new Foo("bar")),
				MediaType.APPLICATION_OCTET_STREAM);
		assertEquals(
				"{\"Foo\":{\"@class\":\"org.springframework.integration.x.bus.MessageBusSupportTests$Foo\",\"bar\":\"bar\"}}",
				new String((byte[]) converted.getPayload()));
		assertEquals(MessageBusSupport.XD_JSON_OCTET_STREAM_VALUE,
				converted.getHeaders().get(MessageHeaders.CONTENT_TYPE));
		MediaType type = new MediaType("application", "x-java-object", Collections.singletonMap("type",
				"org.springframework.integration.x.bus.MessageBusSupportTests$Foo"));
		Message<?> reconstructed = messageBus.transformInboundIfNecessary(converted,
				Collections.singletonList(type));
		assertEquals("bar", ((Foo) reconstructed.getPayload()).getBar());
		assertNull(reconstructed.getHeaders().get(MessageHeaders.CONTENT_TYPE));
	}

	@Test
	public void testJsonTuple() {
		Tuple payload = TupleBuilder.tuple().of("foo", "bar");
		Message<?> converted = messageBus.transformOutboundIfNecessary(new GenericMessage<Tuple>(payload),
				MediaType.APPLICATION_OCTET_STREAM);
		assertEquals(MessageBusSupport.XD_JSON_OCTET_STREAM_VALUE,
				converted.getHeaders().get(MessageHeaders.CONTENT_TYPE));
		Message<?> reconstructed = messageBus.transformInboundIfNecessary(converted,
				Collections.singletonList(MediaType.ALL));
		assertEquals("bar", ((Tuple) reconstructed.getPayload()).getString("foo"));
		assertNull(reconstructed.getHeaders().get(MessageHeaders.CONTENT_TYPE));
	}

	/*
	 * Foo transported as JSON, decoded and then converted to Bar using higher level protected methods
	 */
	@Test
	public void testJsonPojoConvertMessage() {
		DefaultConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new Converter<Foo, Bar>() {

			@Override
			public Bar convert(Foo source) {
				return new Bar(source.getBar());
			}
		});
		messageBus.setConversionService(conversionService);

		Message<?> message = new GenericMessage<Foo>(new Foo("bar"));
		Message<?> messageToSend = messageBus.transformOutboundIfNecessary(message,
				MediaType.APPLICATION_OCTET_STREAM);
		assertEquals(
				"{\"Foo\":{\"@class\":\"org.springframework.integration.x.bus.MessageBusSupportTests$Foo\",\"bar\":\"bar\"}}",
				new String((byte[]) messageToSend.getPayload()));

		MediaType type = new MediaType("application", "x-java-object", Collections.singletonMap("type",
				"org.springframework.integration.x.bus.MessageBusSupportTests$Bar"));
		Message<?> messageToSink = messageBus.transformInboundIfNecessary(messageToSend,
				Collections.singletonList(type));
		assertEquals("bar", ((Bar) messageToSink.getPayload()).getFoo());
	}

	public static class Foo {

		private String bar;

		public Foo() {
		}

		public Foo(String bar) {
			this.bar = bar;
		}

		public String getBar() {
			return bar;
		}

		public void setBar(String bar) {
			this.bar = bar;
		}

	}

	public static class Bar {

		private String foo;

		public Bar() {
		}

		public Bar(String foo) {
			this.foo = foo;
		}

		public String getFoo() {
			return foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}

	}

	public class TestMessageBus extends MessageBusSupport {

		@Override
		public void bindConsumer(String name, MessageChannel channel, Collection<MediaType> acceptedMediaTypes,
				boolean aliasHint) {
		}

		@Override
		public void bindPubSubConsumer(String name, MessageChannel moduleInputChannel,
				Collection<MediaType> acceptedMediaTypes) {
		}

		@Override
		public void bindPubSubProducer(String name, MessageChannel moduleOutputChannel) {
		}

		@Override
		public void bindProducer(String name, MessageChannel channel, boolean aliasHint) {
		}
	}

}
